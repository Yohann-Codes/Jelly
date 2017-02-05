package group;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.Future;
import operation.GroupDao;
import operation.UserDao;
import org.apache.log4j.Logger;
import pojo.Member;
import pojo.User;
import protocol.MessageHolder;
import protocol.ProtocolHeader;

import java.util.List;

/**
 * 讨论组添加成员服务.
 *
 * @author Yohann.
 */
public class MemberAdd {
    private static final Logger logger = Logger.getLogger(MemberAdd.class);

    private Channel channel;
    private String username;
    private String member;
    private String groupName;

    public MemberAdd(Member aMember, Channel channel) {
        this.channel = channel;
        username = aMember.getUsername();
        member = aMember.getMember();
        groupName = aMember.getGroupName();
    }

    public void deal() {
        UserDao userDao = null;
        GroupDao groupDao = null;
        try {
            userDao = new UserDao();
            groupDao = new GroupDao();
            userDao.connect();
            groupDao.connect();
            // 讨论组是否存在
            List<String> members = groupDao.queryMemberByGroupName(groupName);
            if (members != null) {
                // 用户是否存在
                List<User> users = userDao.queryByUsername(member);
                if (users.size() == 1) {
                    // 邀请者是否为创建者
                    if (members.get(0).equals(username)) {
                        // 添加成员
                        String column = groupDao.queryNoMemColumn(groupName);
                        int r = groupDao.insertMember(groupName, member, column);
                        if (r == 1) {
                            success();
                            logger.info("邀请进入讨论组<" + groupName + "> " + username + "-->" + member + " 成功");
                        }
                    } else {
                        defeat(ProtocolHeader.REQUEST_ERROR);
                        logger.info("邀请进入讨论组<" + groupName + "> " + username + "-->" + member + " 失败（非讨论组创建者不能邀请成员）");
                    }
                } else {
                    defeat(ProtocolHeader.REQUEST_ERROR);
                    logger.info("邀请进入讨论组<" + groupName + "> " + username + "-->" + member + " 失败（用户不存在）");
                }
            } else {
                defeat(ProtocolHeader.REQUEST_ERROR);
                logger.info("邀请进入讨论组<" + groupName + "> " + username + "-->" + member + " 失败（讨论组不存在）");
            }
        } finally {
            if (userDao != null) {
                userDao.close();
            }
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
        messageHolder.setType(ProtocolHeader.ADD_MEMBER);
        messageHolder.setStatus(status);
        messageHolder.setBody(body);
        return channel.writeAndFlush(messageHolder);
    }
}
