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
 * 创建讨论组服务.
 *
 * @author Yohann.
 */
public class CreateGroup {
    private static final Logger logger = Logger.getLogger(CreateGroup.class);

    private Channel channel;
    private String username;
    private String groupName;

    public CreateGroup(Group cGroup, Channel channel) {
        this.channel = channel;
        username = cGroup.getUsername();
        groupName = cGroup.getGroupName();
    }

    public void deal() {
        // 讨论组名称唯一
        GroupDao groupDao = null;
        try {
            groupDao = new GroupDao();
            groupDao.connect();
            List<String> members = groupDao.queryMemberByGroupName(groupName);
            if (members == null) {
                int r = groupDao.insertGroup(groupName, username);
                if (r == 1) {
                    success();
                    logger.info(username + " 创建讨论组 " + groupName + " 成功");
                } else {
                    defeat(ProtocolHeader.SERVER_ERROR);
                    logger.warn("数据库错误");
                }
            } else {
                // 讨论组名字已存在
                defeat(ProtocolHeader.REQUEST_ERROR);
                logger.info(username + " 创建讨论组 " + groupName + " 失败（讨论组名称已存在）");
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
        messageHolder.setType(ProtocolHeader.CREATE_GROUP);
        messageHolder.setStatus(status);
        messageHolder.setBody(body);
        return channel.writeAndFlush(messageHolder);
    }
}
