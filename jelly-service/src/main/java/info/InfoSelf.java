package info;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.Future;
import json.Serializer;
import operation.UserDao;
import org.apache.log4j.Logger;
import pojo.Info;
import pojo.User;
import protocol.MessageHolder;
import protocol.ProtocolHeader;

import java.util.List;

/**
 * 查看个人信息服务.
 *
 * @author Yohann.
 */
public class InfoSelf {
    private static final Logger logger = Logger.getLogger(InfoSelf.class);

    private Channel channel;
    private Info info;

    public InfoSelf(Info info, Channel channel) {
        this.channel = channel;
        this.info = info;
    }

    public void deal() {
        UserDao userDao = null;
        String username = info.getUsername();
        try {
            userDao = new UserDao();
            userDao.connect();
            List<User> users = userDao.queryByUsername(username);
            if (users.size() == 1) {
                User user = users.get(0);
                success(user);
                logger.info(username + "查询个人信息 成功");
            } else {
                // 数据库异常
                defeat(ProtocolHeader.SERVER_ERROR);
                logger.warn("查询个人信息 数据库异常");
            }
        } finally {
            if (userDao != null) {
                userDao.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void success(User user) {
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
        messageHolder.setType(ProtocolHeader.LOOK_SELF_INFO);
        messageHolder.setStatus(status);
        messageHolder.setBody(body);
        return channel.writeAndFlush(messageHolder);
    }
}
