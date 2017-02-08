package service;

import account.Login;
import account.Logout;
import account.Reconn;
import account.Register;
import friend.AllFriend;
import friend.FriendAdd;
import friend.FriendRemove;
import group.CreateGroup;
import group.DisbandGroup;
import group.MemberAdd;
import group.MemberRemove;
import info.InfoFriend;
import info.InfoGroup;
import info.InfoSelf;
import info.InfoUpdate;
import io.netty.channel.Channel;
import io.netty.util.ReferenceCountUtil;
import json.Serializer;
import message.GroupMessage;
import message.PersonMessage;
import pojo.*;
import protocol.MessageHolder;
import protocol.ProtocolHeader;

/**
 * 业务分发器.
 *
 * @author Yohann.
 */
public class Dispatcher {

    public static void dispatch(MessageHolder messageHolder) {

        if (messageHolder.getSign() != ProtocolHeader.REQUEST) {
            // 请求错误
            response(messageHolder.getChannel(), messageHolder.getSign());
            return;
        }

        switch (messageHolder.getType()) {
            // 登录
            case ProtocolHeader.LOGIN:
                Account aLogin = Serializer.deserialize(messageHolder.getBody(), Account.class);
                new Login(aLogin, messageHolder.getChannel()).deal();
                break;

            // 登出
            case ProtocolHeader.LOGOUT:
                Account aLogout = Serializer.deserialize(messageHolder.getBody(), Account.class);
                new Logout(aLogout, messageHolder.getChannel()).deal();
                break;

            // 注册
            case ProtocolHeader.REGISTER:
                Account aRegister = Serializer.deserialize(messageHolder.getBody(), Account.class);
                new Register(aRegister, messageHolder.getChannel()).deal();
                break;

            // 个人消息
            case ProtocolHeader.PERSON_MESSAGE:
                Message pMessage = Serializer.deserialize(messageHolder.getBody(), Message.class);
                new PersonMessage(pMessage, messageHolder.getChannel()).deal();
                break;

            // 添加好友
            case ProtocolHeader.ADD_FRIEND:
                Friend aFriend = Serializer.deserialize(messageHolder.getBody(), Friend.class);
                new FriendAdd(aFriend, messageHolder.getChannel()).deal();
                break;

            // 删除好友
            case ProtocolHeader.REMOVE_FRIEND:
                Friend rFriend = Serializer.deserialize(messageHolder.getBody(), Friend.class);
                new FriendRemove(rFriend, messageHolder.getChannel()).deal();
                break;

            // 查询已添加好友
            case ProtocolHeader.ALL_FRIEND:
                MyFriend myFriend = Serializer.deserialize(messageHolder.getBody(), MyFriend.class);
                new AllFriend(myFriend, messageHolder.getChannel()).deal();
                break;

            // 修改个人信息
            case ProtocolHeader.UPDATE_SELF_INFO:
                Info uInfo = Serializer.deserialize(messageHolder.getBody(), Info.class);
                new InfoUpdate(uInfo, messageHolder.getChannel()).deal();
                break;

            // 查询个人信息
            case ProtocolHeader.LOOK_SELF_INFO:
                Info mInfo = Serializer.deserialize(messageHolder.getBody(), Info.class);
                new InfoSelf(mInfo, messageHolder.getChannel()).deal();
                break;

            // 查询好友个人信息
            case ProtocolHeader.LOOK_FRIEND_INFO:
                Friend friend = Serializer.deserialize(messageHolder.getBody(), Friend.class);
                new InfoFriend(friend, messageHolder.getChannel()).deal();
                break;

            // 创建讨论组
            case ProtocolHeader.CREATE_GROUP:
                Group cGroup = Serializer.deserialize(messageHolder.getBody(), Group.class);
                new CreateGroup(cGroup, messageHolder.getChannel()).deal();
                break;

            // 解散讨论组
            case ProtocolHeader.DISBAND_GROUP:
                Group dGroup = Serializer.deserialize(messageHolder.getBody(), Group.class);
                new DisbandGroup(dGroup, messageHolder.getChannel()).deal();
                break;

            // 讨论组添加成员
            case ProtocolHeader.ADD_MEMBER:
                Member aMember = Serializer.deserialize(messageHolder.getBody(), Member.class);
                new MemberAdd(aMember, messageHolder.getChannel()).deal();
                break;

            // 讨论组删除成员
            case ProtocolHeader.REMOVE_MEMBER:
                Member rMember = Serializer.deserialize(messageHolder.getBody(), Member.class);
                new MemberRemove(rMember, messageHolder.getChannel()).deal();
                break;

            // 讨论组消息
            case ProtocolHeader.GROUP_MESSAGE:
                Message gMessage = Serializer.deserialize(messageHolder.getBody(), Message.class);
                new GroupMessage(gMessage, messageHolder.getChannel()).deal();
                break;

            // 查看所在讨论组
            case ProtocolHeader.LOOK_GROUP_INFO:
                MyGroup myGroup = Serializer.deserialize(messageHolder.getBody(), MyGroup.class);
                new InfoGroup(myGroup, messageHolder.getChannel()).deal();
                break;

            // 断线重连
            case ProtocolHeader.RECONN:
                Account reconn = Serializer.deserialize(messageHolder.getBody(), Account.class);
                new Reconn(reconn, messageHolder.getChannel()).deal();
                break;

            // 请求错误
            default:
                response(messageHolder.getChannel(), messageHolder.getSign());
                break;
        }

        // 释放buffer
        ReferenceCountUtil.release(messageHolder);
    }

    /**
     * 请求错误响应
     *
     * @param channel
     * @param sign
     */
    private static void response(Channel channel, byte sign) {
        MessageHolder messageHolder = new MessageHolder();
        messageHolder.setSign(ProtocolHeader.RESPONSE);
        messageHolder.setType(sign);
        messageHolder.setStatus(ProtocolHeader.REQUEST_ERROR);
        messageHolder.setBody("");
        channel.writeAndFlush(messageHolder);
    }
}
