package friend;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.Future;
import operation.FriendDao;
import org.apache.log4j.Logger;
import pojo.Friend;
import protocol.MessageHolder;
import protocol.ProtocolHeader;

/**
 * 删除好友服务.
 *
 * @author Yohann.
 */
public class FriendRemove {
    private static final Logger logger = Logger.getLogger(FriendRemove.class);

    private Channel channel;
    private String username;
    private String friend;

    public FriendRemove(Friend rFriend, Channel channel) {
        this.channel = channel;
        username = rFriend.getUsername();
        friend = rFriend.getFriend();
    }

    public void deal() {
        FriendDao friendDao = null;

        try {
            friendDao = new FriendDao();
            friendDao.connect();
            // 删除好友
            String c = friendDao.queryColumnByFri(username, friend);
            if (c != null) {
                // 删除
                int r = friendDao.removeFriend(username, c);
                if (r == 1) {
                    logger.info(username + " 删除好友 " + friend + " 操作成功");
                    success();
                } else {
                    logger.warn(username + " 删除好友 " + friend + " 操作失败(数据库错误)");
                    defeat(ProtocolHeader.SERVER_ERROR);
                }
            } else {
                // 无此好友
                logger.info(username + " 删除好友 " + friend + " 操作失败（没有添加该好友）");
                defeat(ProtocolHeader.REQUEST_ERROR);
            }
        } finally {
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
        messageHolder.setType(ProtocolHeader.REMOVE_FRIEND);
        messageHolder.setStatus(status);
        messageHolder.setBody(body);
        return channel.writeAndFlush(messageHolder);
    }
}
