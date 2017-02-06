package message;

import connection.ConnPool;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.Future;
import json.Serializer;
import operation.GroupDao;
import operation.GroupMsgDao;
import operation.UserDao;
import org.apache.log4j.Logger;
import pojo.Message;
import protocol.MessageHolder;
import protocol.ProtocolHeader;
import service.GroupManager;

import java.sql.Timestamp;
import java.util.List;

/**
 * 讨论组消息服务.
 *
 * @author Yohann.
 */
public class GroupMessage {
    private static final Logger logger = Logger.getLogger(GroupMessage.class);

    private Channel channel;
    private String sender;
    private String groupName;
    private String content;
    private Long time;

    // 发送结果，只要有一个发送失败就置为false
    private boolean r1 = true, r2 = true;

    public GroupMessage(Message message, Channel channel) {
        this.channel = channel;
        sender = message.getSender();
        groupName = message.getReceiver();
        content = message.getContent();
        time = message.getTime();
    }

    public void deal() {
        // 先在内存中找group
        List<String> members = GroupManager.groupsQuery(groupName);
        if (members != null) {
            // 修改讨论组最后一次活跃时间
            GroupManager.groupTimesUpdate(groupName, time);
            logger.info("内存 讨论组<" + groupName + "> 修改时间戳");
            for (int i = 0; i < members.size(); i++) {
                String member = members.get(i);
                if (!member.equals(sender)) {
                    sendMessage(member);
                }
            }
        } else {
            // 查询数据库
            GroupDao groupDao = null;
            try {
                groupDao = new GroupDao();
                groupDao.connect();
                members = groupDao.queryMemberByGroupName(groupName);
                if (members != null) {
                    // 添加讨论组
                    GroupManager.groupsAdd(groupName, members);
                    GroupManager.groupTimesAdd(groupName, time);
                    logger.info("数据库 讨论组<" + groupName + "> 维护在内存");
                    for (int i = 0; i < members.size(); i++) {
                        String member = members.get(i);
                        if (!member.equals(sender)) {
                            sendMessage(member);
                        }
                    }
                } else {
                    // 讨论组不存在
                    logger.info("讨论组消息 " + sender + "-->" + groupName + " 发送失败（讨论组不存在）");
                    r1 = false;
                }
            } finally {
                if (groupDao != null) {
                    groupDao.close();
                }
            }
        }

        // 发送响应
        if (r1 && r2) {
            sendResponse(ProtocolHeader.SUCCESS, "");
        } else {
            if (!r1) {
                sendResponse(ProtocolHeader.REQUEST_ERROR, "");
            }
            if (!r2) {
                sendResponse(ProtocolHeader.SERVER_ERROR, "");
            }
        }
    }

    /**
     * 发送消息
     *
     * @param member
     */
    private void sendMessage(String member) {
        // 在Online用户中查找，ConnPool
        Channel recChannel = ConnPool.query(member);
        if (recChannel != null) {
            online(recChannel);
        } else {
            // 在Offline用户中查找，数据库
            offline(member);
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
        message.setReceiver(groupName);
        message.setContent(content);
        message.setTime(time);
        String body = Serializer.serialize(message);
        Future future = send(recChannel, body);
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                logger.info("讨论组在线消息 " + sender + "-->" + groupName + "-->" + ConnPool.query(recChannel) + " 转发成功");
            }
        });
    }

    /**
     * 离线消息
     *
     * @param member
     */
    private void offline(String member) {
        UserDao userDao = null;
        GroupMsgDao groupMsgDao = null;
        try {
            userDao = new UserDao();
            groupMsgDao = new GroupMsgDao();
            userDao.connect();
            groupMsgDao.connect();
            int row = groupMsgDao
                    .insertMsg(sender, member, groupName, content, new Timestamp(time));
            if (row == 1) {
                logger.info("讨论组离线消息 " + sender + "-->" + groupName + "-->" + member + " 存储成功");
            } else {
                logger.warn("数据库错误");
                r2 = false;
            }
        } finally {
            if (userDao != null) {
                userDao.close();
            }
            if (groupMsgDao != null) {
                groupMsgDao.close();
            }
        }
    }

    private Future send(Channel recChannel, String body) {
        MessageHolder messageHolder = new MessageHolder();
        messageHolder.setSign(ProtocolHeader.NOTICE);
        messageHolder.setType(ProtocolHeader.GROUP_MESSAGE);
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
        messageHolder.setType(ProtocolHeader.GROUP_MESSAGE);
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
