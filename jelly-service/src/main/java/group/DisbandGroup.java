package group;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.Future;
import operation.GroupDao;
import org.apache.log4j.Logger;
import pojo.Group;
import protocol.MessageHolder;
import protocol.ProtocolHeader;

import java.util.List;

/**
 * 解散讨论组服务.
 *
 * @author Yohann.
 */
public class DisbandGroup {
    private static final Logger logger = Logger.getLogger(DisbandGroup.class);

    private Channel channel;
    private String username;
    private String groupName;

    public DisbandGroup(Group cGroup, Channel channel) {
        this.channel = channel;
        username = cGroup.getUsername();
        groupName = cGroup.getGroupName();
    }

    public void deal() {
        // 查询讨论组是否存在
        GroupDao groupDao = null;
        try {
            groupDao = new GroupDao();
            groupDao.connect();
            List<String> members = groupDao.queryMemberByGroupName(groupName);
            if (members != null) {
                if (members.get(0).equals(username)) {
                    int r = groupDao.removeGroup(groupName);
                    if (r == 1) {
                        success();
                        logger.info(username + " 解散讨论组 " + groupName + " 成功");
                    } else {
                        defeat(ProtocolHeader.SERVER_ERROR);
                        logger.warn("数据库错误");
                    }
                } else {
                    // 非创建者操作
                    defeat(ProtocolHeader.REQUEST_ERROR);
                    logger.info(username + " 解散讨论组 " + groupName + " 失败(非讨论创建者不能执行解散操作)");
                }
            } else {
                // 讨论组不存在
                defeat(ProtocolHeader.REQUEST_ERROR);
                logger.info(username + " 解散讨论组 " + groupName + " 失败（讨论组不存在）");
            }
        } finally {
            if (groupDao != null) {
                groupDao.close();
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
        messageHolder.setType(ProtocolHeader.DISBAND_GROUP);
        messageHolder.setStatus(status);
        messageHolder.setBody(body);
        return channel.writeAndFlush(messageHolder);
    }
}
