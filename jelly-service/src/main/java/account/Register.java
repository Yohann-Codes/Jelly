package account;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.Future;
import json.Serializer;
import operation.FriendDao;
import operation.UserDao;
import org.apache.log4j.Logger;
import pojo.Account;
import pojo.User;
import protocol.MessageHolder;
import protocol.ProtocolHeader;

import java.util.List;

/**
 * 注册服务.
 *
 * @author Yohann.
 */
public class Register {
    private static final Logger logger = Logger.getLogger(Register.class);

    private String username;
    private String password;
    private Channel channel;
    private Account account;

    public Register(Account account, Channel channel) {
        username = account.getUsername();
        password = account.getPassword();
        this.account = account;
        this.channel = channel;
    }

    public void deal() {
        UserDao userDao = null;
        FriendDao friendDao = null;
        // 查询用户名是否已存在
        try {
            userDao = new UserDao();
            friendDao = new FriendDao();
            userDao.connect();
            friendDao.connect();
            List<User> users = userDao.queryByUsername(username);
            if (users.size() == 0) {
                // 添加用户
                int r1 = userDao.insertUser(username, password);
                int r2 = friendDao.insertAccount(username);
                if (r1 == 1 && r2 == 1) {
                    // 成功
                    success();
                } else {
                    // 失败，数据库错误
                    defeat(ProtocolHeader.SERVER_ERROR);
                    logger.warn("注册时数据库出现错误");
                }
            } else {
                // 失败，用户名已存在
                defeat(ProtocolHeader.REQUEST_ERROR);
            }
        } finally {
            if (userDao != null) {
                userDao.close();
            }
            if (friendDao != null) {
                friendDao.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void success() {
        // 返回用户名
        Account account = new Account();
        account.setUsername(username);
        Future future = sendResponse(ProtocolHeader.SUCCESS, Serializer.serialize(account));
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    // 关闭连接
                    channel.close().sync();
                } else {
                    sendResponse(ProtocolHeader.SUCCESS, Serializer.serialize(account))
                            .addListener(new ChannelFutureListener() {
                                @Override
                                public void operationComplete(ChannelFuture future) throws Exception {
                                    if (future.isSuccess()) {
                                        // 关闭连接
                                        channel.close().sync();
                                    }
                                }
                            });
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void defeat(byte status) {
        Future future = sendResponse(status, "");
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    // 关闭连接
                    channel.close().sync();
                } else {
                    sendResponse(status, "").addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (future.isSuccess()) {
                                // 关闭连接
                                channel.close().sync();
                            }
                        }
                    });
                }
            }
        });
    }

    private Future sendResponse(byte status, String body) {
        MessageHolder messageHolder = new MessageHolder();
        messageHolder.setSign(ProtocolHeader.RESPONSE);
        messageHolder.setType(ProtocolHeader.REGISTER);
        messageHolder.setStatus(status);
        messageHolder.setBody(body);
        return channel.writeAndFlush(messageHolder);
    }
}
