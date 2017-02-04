package service;

import account.Login;
import account.Logout;
import account.Register;
import io.netty.channel.Channel;
import io.netty.util.ReferenceCountUtil;
import json.Serializer;
import pojo.Account;
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
