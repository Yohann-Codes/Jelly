package info;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.Future;
import json.Serializer;
import operation.FriendDao;
import operation.UserDao;
import org.apache.log4j.Logger;
import pojo.Friend;
import pojo.Info;
import pojo.User;
import protocol.MessageHolder;
import protocol.ProtocolHeader;

import java.util.List;

/**
 * 查看好友个人信息服务.
 *
 * @author Yohann.
 */
public class InfoFriend {
    private static final Logger logger = Logger.getLogger(InfoFriend.class);

    private Channel channel;
    private Friend friend;

    public InfoFriend(Friend friend, Channel channel) {
        this.channel = channel;
        this.friend = friend;
    }

    public void deal() {
        // 查询是否为好友
        UserDao userDao = null;
        FriendDao friendDao = null;
        try {
            userDao = new UserDao();
            friendDao = new FriendDao();
            userDao.connect();
            friendDao.connect();
            String c = friendDao.queryColumnByFri(friend.getUsername(), friend.getFriend());
            if (c != null) {
                // 查询信息
                List<User> users = userDao.queryByUsername(friend.getFriend());
                if (users.size() == 1) {
                    User user = users.get(0);
                    success(user);
                    logger.info(friend.getUsername() + "查询好友信息(" + friend.getFriend() + ") 成功");
                } else {
                    // 数据库异常
                    defeat(ProtocolHeader.SERVER_ERROR);
                    logger.warn("查询好友信息 数据库异常");
                }
            } else {
                // 未添加好友
                defeat(ProtocolHeader.REQUEST_ERROR);
                logger.info("查询好友信息 失败 不是好友");
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
    private void success(User user) {
        Info info = new Info();
        info.setUsername(user.getUsername());
        info.setName(user.getName());
        info.setSex(user.getSex());
        info.setAge(user.getAge());
        info.setPhone(user.getPhone());
        info.setAddress(user.getAddress());
        info.setIntroduction(user.getIntroduction());
        String body = Serializer.serialize(info);
        Future future = sendResponse(ProtocolHeader.SUCCESS, body);
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    sendResponse(ProtocolHeader.SUCCESS, body);
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
                if (!future.isSuccess()) {
                    sendResponse(status, "");
                }
            }
        });
    }

    private Future sendResponse(byte status, String body) {
        MessageHolder messageHolder = new MessageHolder();
        messageHolder.setSign(ProtocolHeader.RESPONSE);
        messageHolder.setType(ProtocolHeader.LOOK_FRIEND_INFO);
        messageHolder.setStatus(status);
        messageHolder.setBody(body);
        return channel.writeAndFlush(messageHolder);
    }
}
