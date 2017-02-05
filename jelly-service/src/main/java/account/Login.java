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
import operation.MsgDao;
import operation.UserDao;
import org.apache.log4j.Logger;
import pojo.Account;
import pojo.Message;
import pojo.OfflineMessage;
import pojo.User;
import protocol.MessageHolder;
import protocol.ProtocolHeader;

import java.sql.SQLException;
import java.util.List;

/**
 * 登录服务.
 * <p>
 *
 * @author Yohann.
 */
public class Login {
    private static final Logger logger = Logger.getLogger(Login.class);

    private Channel channel;
    private String username;
    private String password;

    public Login(Account account, Channel channel) {
        username = account.getUsername();
        password = account.getPassword();
        this.channel = channel;
    }

    /**
     * 登录信息验证
     */
    public void deal() {
        UserDao userDao = null;
        try {
            userDao = new UserDao();
            userDao.connect();
            List<User> users = userDao.queryByUsername(username);
            if (users.size() == 1) {
                if (password.equals(users.get(0).getPassword())) {
                    // 成功
                    success();
                } else {
                    // 失败，密码错误
                    defeat(ProtocolHeader.REQUEST_ERROR);
                }
            } else {
                // 失败，用户名错误
                defeat(ProtocolHeader.REQUEST_ERROR);
            }
        } finally {
            if (userDao != null) {
                userDao.close();
            }
        }
    }

    /**
     * 信息验证成功
     */
    @SuppressWarnings("unchecked")
    private void success() {
        Long token = init();
        // 发送响应数据包
        Account acc = new Account();
        acc.setUsername(username);
        acc.setToken(token);
        Future future = sendResponse(ProtocolHeader.SUCCESS, Serializer.serialize(acc));
        future.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    logger.info(username + " 登录成功");
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
                                        logger.info(username + " 登录成功");
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
                    logger.info(username + " 登录失败");
                    channel.close().sync();
                } else {
                    sendResponse(status, "").addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (future.isSuccess()) {
                                logger.info(username + " 登录失败");
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
        messageHolder.setType(ProtocolHeader.LOGIN);
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
                    sendMessage(channel, Serializer.serialize(message));
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

    private void groupMessage() {

    }

    /**
     * 发送消息
     *
     * @param recChannel
     * @param body
     * @return
     */
    private Future sendMessage(Channel recChannel, String body) {
        MessageHolder messageHolder = new MessageHolder();
        messageHolder.setSign(ProtocolHeader.NOTICE);
        messageHolder.setType(ProtocolHeader.PERSON_MESSAGE);
        messageHolder.setStatus((byte) 0);
        messageHolder.setBody(body);
        return recChannel.writeAndFlush(messageHolder);
    }
}
