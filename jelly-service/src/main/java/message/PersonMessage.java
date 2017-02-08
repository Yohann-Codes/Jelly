package message;

import connection.ConnPool;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.Future;
import json.Serializer;
import operation.MsgDao;
import operation.UserDao;
import org.apache.log4j.Logger;
import pojo.Message;
import pojo.User;
import protocol.MessageHolder;
import protocol.ProtocolHeader;

import java.sql.Timestamp;
import java.util.List;

/**
 * 个人消息服务.
 *
 * @author Yohann.
 */
public class PersonMessage {
    private static final Logger logger = Logger.getLogger(PersonMessage.class);

    private Channel channel;
    private String sender;
    private String receiver;
    private String content;
    private Long time;

    public PersonMessage(Message message, Channel channel) {
        this.channel = channel;
        sender = message.getSender();
        receiver = message.getReceiver();
        content = message.getContent();
        time = message.getTime();
    }

    public void deal() {
        // 在Online用户中查找，ConnPool
        Channel recChannel = ConnPool.query(receiver);
        if (recChannel != null) {
            online(recChannel);
        } else {
            // 在Offline用户中查找，数据库
            offline();
        }
    }

    /**
     * 在线消息
     *
     * @param recChannel
     */
    @SuppressWarnings("unchecked")
    private void online(Channel recChannel) {
        // 转发消息
        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);
        message.setTime(time);
        String body = Serializer.serialize(message);
        Future future = sendMessage(recChannel, body);
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    // 发送响应
                    sendResponse(ProtocolHeader.SUCCESS, "");
                    logger.info("个人消息(online) " + sender + "-->" + receiver + " 成功");
                } else {
                    sendMessage(recChannel, body).addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (future.isSuccess()) {
                                // 发送响应
                                sendResponse(ProtocolHeader.SUCCESS, "");
                                logger.info("个人消息(online) " + sender + "-->" + receiver + " 成功");
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * 离线消息
     */
    private void offline() {
        UserDao userDao = null;
        MsgDao msgDao = null;
        try {
            userDao = new UserDao();
            msgDao = new MsgDao();
            userDao.connect();
            msgDao.connect();
            // 查询是否存在该用户
            List<User> users = userDao.queryByUsername(receiver);
            if (users.size() == 1) {
                int row = msgDao
                        .insertMsg(sender, receiver, content, new Timestamp(time));
                if (row == 1) {
                    sendResponse(ProtocolHeader.SUCCESS, "");
                    logger.info("个人消息(offline) " + sender + "-->" + receiver + " 成功");
                } else {
                    sendResponse(ProtocolHeader.SERVER_ERROR, "");
                    logger.info("个人消息(online) " + sender + "-->" + receiver + " 失败(数据库错误)");
                }
            } else {
                sendResponse(ProtocolHeader.SERVER_ERROR, "");
                logger.info("个人消息(online) " + sender + "-->" + receiver + " 失败(数据库错误)");
            }
        } finally {
            if (userDao != null) {
                userDao.close();
            }
            if (msgDao != null) {
                msgDao.close();
            }
        }
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

    /**
     * 发送响应
     *
     * @param status
     * @param body
     * @return
     */
    private void sendResponse(byte status, String body) {
        MessageHolder messageHolder = new MessageHolder();
        messageHolder.setSign(ProtocolHeader.RESPONSE);
        messageHolder.setType(ProtocolHeader.PERSON_MESSAGE);
        messageHolder.setStatus(status);
        messageHolder.setBody(body);
        ChannelFuture future = channel.writeAndFlush(messageHolder);
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    sendResponse(status, "");
                }
            }
        });
    }
}
