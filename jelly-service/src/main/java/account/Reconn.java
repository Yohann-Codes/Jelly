package account;

import connection.ConnPool;
import connection.TokenFactory;
import connection.TokenPool;
import handler.HeartbeatHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.Future;
import json.Serializer;
import operation.GroupMsgDao;
import operation.MsgDao;
import org.apache.log4j.Logger;
import pojo.*;
import protocol.MessageHolder;
import protocol.ProtocolHeader;

import java.util.List;

/**
 * 断线重连服务.
 *
 * @author Yohann.
 */
public class Reconn {
    private static final Logger logger = Logger.getLogger(Reconn.class);

    private Channel channel;
    private String username;
    private Long token;

    public Reconn(Account account, Channel channel) {
        username = account.getUsername();
        token = account.getToken();
        this.channel = channel;
    }

    /**
     * 登录信息验证
     */
    public void deal() {
        // 验证token
        if (TokenPool.query(token)) {
            success();
        } else {
            // token验证失败
            defeat(ProtocolHeader.REQUEST_ERROR);
            logger.info("token验证失败，拒绝重连");
        }
    }

    /**
     * 信息验证成功
     */
    @SuppressWarnings("unchecked")
    private void success() {
        // 维护连接
        boolean b = ConnPool.add(username, channel);
        // 发送响应数据包
        Account acc = new Account();
        acc.setUsername(username);
        Future future = sendResponse(ProtocolHeader.SUCCESS, Serializer.serialize(acc));
        future.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    logger.info(username + " 重连成功");
                    // 开启心跳检测
                    logger.info(username + " 开启心跳检测");
                    channel.pipeline().addAfter("IdleStateHandler",
                            "HeartbeatHandler", new HeartbeatHandler(channel));

                    // 发送离线消息
                    sendOfflineMessage();

                } else {
                    sendResponse(ProtocolHeader.SUCCESS, Serializer.serialize(acc))
                            .addListener(new ChannelFutureListener() {
                                @Override
                                public void operationComplete(ChannelFuture future) throws Exception {
                                    if (future.isSuccess()) {
                                        logger.info(username + " 重连成功");
                                        // 开启心跳检测
                                        logger.info(username + " 开启心跳检测");
                                        channel.pipeline().addAfter("IdleStateHandler",
                                                "HeartbeatHandler", new HeartbeatHandler(channel));

                                        // 发送离线消息
                                        sendOfflineMessage();
                                    }
                                }
                            });
                }
            }
        });
    }

    /**
     * 信息验证失败
     *
     * @param status
     */
    @SuppressWarnings("unchecked")
    private void defeat(byte status) {
        // 发送响应数据包
        Future future = sendResponse(status, "");
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    logger.info(username + " 重连失败");
                    channel.close().sync();
                } else {
                    sendResponse(status, "").addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (future.isSuccess()) {
                                logger.info(username + " 重连失败");
                                channel.close().sync();
                            }
                        }
                    });
                }

            }
        });
    }

    /**
     * 登录信息验证成功后的初始化
     *
     * @return
     */
    private Long init() {
        // 生成token
        TokenFactory factory = new TokenFactory();
        Long token = factory.generate();
        // 维护连接
        ConnPool.add(username, channel);
        // 维护token
        TokenPool.add(token);
        return token;
    }

    private Future sendResponse(byte status, String body) {
        MessageHolder messageHolder = new MessageHolder();
        messageHolder.setSign(ProtocolHeader.RESPONSE);
        messageHolder.setType(ProtocolHeader.RECONN);
        messageHolder.setStatus(status);
        messageHolder.setBody(body);
        return channel.writeAndFlush(messageHolder);
    }

    private void sendOfflineMessage() {
        // 个人消息
        personMessage();
        // 讨论组消息
        groupMessage();
    }

    /**
     * 发送个人离线消息
     */
    private void personMessage() {
        MsgDao msgDao = null;
        try {
            msgDao = new MsgDao();
            msgDao.connect();
            // 查询消息
            List<OfflineMessage> offlineMsgs = msgDao.queryMsg(username);
            if (offlineMsgs.size() != 0) {
                // 一个一个发送
                for (int i = 0; i < offlineMsgs.size(); i++) {
                    OfflineMessage offlineMessage = offlineMsgs.get(i);
                    Message message = new Message();
                    message.setSender(offlineMessage.getSender());
                    message.setReceiver(offlineMessage.getReceiver());
                    message.setContent(offlineMessage.getMessage());
                    message.setTime(offlineMessage.getTime());
                    sendMessage(ProtocolHeader.PERSON_MESSAGE, channel, Serializer.serialize(message));
                    logger.info("个人消息(离线) " + message.getSender()
                            + "-->" + message.getReceiver() + " 发送成功");
                }
                // 删除离线消息
                int row = msgDao.removeMsg(username);
                if (row == offlineMsgs.size()) {
                } else {
                    logger.warn("数据库错误");
                }
            } else {
                return;
            }
        } finally {
            if (msgDao != null) {
                msgDao.close();
            }
        }
    }

    /**
     * 发送讨论组离线消息
     */
    private void groupMessage() {
        GroupMsgDao groupMsgDao = null;
        try {
            groupMsgDao = new GroupMsgDao();
            groupMsgDao.connect();
            // 查询消息
            List<OfflineGroupMessage> offlineMsgs = groupMsgDao.queryMsg(username);
            if (offlineMsgs.size() != 0) {
                // 一个一个发送
                for (int i = 0; i < offlineMsgs.size(); i++) {
                    OfflineGroupMessage offlineGroupMessage = offlineMsgs.get(i);
                    Message message = new Message();
                    message.setSender(offlineGroupMessage.getSender());
                    message.setReceiver(offlineGroupMessage.getGroup());
                    message.setContent(offlineGroupMessage.getMessage());
                    message.setTime(offlineGroupMessage.getTime());
                    sendMessage(ProtocolHeader.GROUP_MESSAGE, channel, Serializer.serialize(message));
                    logger.info("讨论组离线消息 " + offlineGroupMessage.getSender()
                            + "-->" + offlineGroupMessage.getGroup() + "-->" + username + " 发送成功");
                }
                // 删除离线消息
                int row = groupMsgDao.removeMsg(username);
                if (row == offlineMsgs.size()) {
                    logger.info("删除讨论组离线消息 成功");
                } else {
                    logger.warn("删除讨论组离线消息 失败");
                }
            } else {
                return;
            }
        } finally {
            if (groupMsgDao != null) {
                groupMsgDao.close();
            }
        }
    }

    /**
     * 发送消息
     *
     * @param type
     * @param recChannel
     * @param body
     * @return
     */
    private Future sendMessage(byte type, Channel recChannel, String body) {
        MessageHolder messageHolder = new MessageHolder();
        messageHolder.setSign(ProtocolHeader.NOTICE);
        messageHolder.setType(type);
        messageHolder.setStatus((byte) 0);
        messageHolder.setBody(body);
        return recChannel.writeAndFlush(messageHolder);
    }
}
