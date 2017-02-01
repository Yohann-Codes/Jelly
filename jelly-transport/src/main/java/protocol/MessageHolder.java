package protocol;

import io.netty.channel.Channel;

import java.util.Arrays;

/**
 * 消息载体.
 *
 * 传输模块与服务模块之间双向数据传输载体:
 *
 *                     MessageHolder
 * Transport Module <----------------> Service Module
 *
 * @author Yohann.
 */
public class MessageHolder {

    // 消息标志
    private byte sign;
    // 消息类型
    private byte type;
    // 响应状态
    private byte status;
    // 字节消息体
    private byte[] bytes;
    // 消息通道
    private Channel channel;

    public byte getSign() {
        return sign;
    }

    public void setSign(byte sign) {
        this.sign = sign;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
