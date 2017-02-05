package friend;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.Future;
import operation.FriendDao;
import operation.UserDao;
import org.apache.log4j.Logger;
import pojo.Friend;
import pojo.User;
import protocol.MessageHolder;
import protocol.ProtocolHeader;

import java.util.List;

/**
 * 添加好友服务.
 *
 * @author Yohann.
 */
public class FriendAdd {
    private static final Logger logger = Logger.getLogger(FriendAdd.class);

    private Channel channel;
    private String username;
    private String friend;

    public FriendAdd(Friend aFriend, Channel channel) {
        this.channel = channel;
        username = aFriend.getUsername();
        friend = aFriend.getFriend();
    }

    public void deal() {
        UserDao userDao = null;
        FriendDao friendDao = null;
        try {
            userDao = new UserDao();
            friendDao = new FriendDao();
            userDao.connect();
            friendDao.connect();

            // 查询是否存在该用户
            List<User> users = userDao.queryByUsername(username);
            if (users.size() == 1) {
                // 查询sender的好友数量是否达到上线
                String column = friendDao.queryNoFriColumn(username);
                if (column != null) {
                    // 添加到好友列表
                    int row = friendDao.insertFriend(username, friend, column);
                    if (row > 0) {
                        success();
                        logger.warn("添加好友 " + username + "-->" + friend + " 添加成功");
                    } else {
                        defeat(ProtocolHeader.SERVER_ERROR);
                        logger.warn("添加好友 " + username + "-->" + friend + " 添加失败(数据库错误)");
                    }
                } else {
                    // 好友数量爆满
                    defeat(ProtocolHeader.REQUEST_ERROR);
                    logger.info("添加好友 " + username + "-->" + friend + " 添加失败（好友数量爆满）");
                }
            } else {
                // 不存在该用户
                defeat(ProtocolHeader.REQUEST_ERROR);
                logger.info("添加好友 " + username + "-->" + friend + " 添加失败（不存在该用户）");
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
        Future future = sendResponse(ProtocolHeader.SUCCESS, "");
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    sendResponse(ProtocolHeader.SUCCESS, "");
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
                    sendResponse(ProtocolHeader.SUCCESS, "");
                }
            }
        });
    }

    private Future sendResponse(byte status, String body) {
        MessageHolder messageHolder = new MessageHolder();
        messageHolder.setSign(ProtocolHeader.RESPONSE);
        messageHolder.setType(ProtocolHeader.ADD_FRIEND);
        messageHolder.setStatus(status);
        messageHolder.setBody(body);
        return channel.writeAndFlush(messageHolder);
    }
}
