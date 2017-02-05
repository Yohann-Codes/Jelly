package handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.ReferenceCountUtil;
import org.apache.log4j.Logger;
import protocol.MessageHolder;
import protocol.ProtocolHeader;
import sun.rmi.runtime.Log;

import java.util.List;

/**
 * 解码Handler.
 *
 *                                       Jelly Protocol
 *  __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __
 * |           |           |           |           |              |                          |
 *       2           1           1           1            4               Uncertainty
 * |__ __ __ __|__ __ __ __|__ __ __ __|__ __ __ __|__ __ __ __ __|__ __ __ __ __ __ __ __ __|
 * |           |           |           |           |              |                          |
 *     Magic        Sign        Type       Status     Body Length         Body Content
 * |__ __ __ __|__ __ __ __|__ __ __ __|__ __ __ __|__ __ __ __ __|__ __ __ __ __ __ __ __ __|
 *
 * 协议头9个字节定长
 *     Magic      // 数据包的验证位，short类型
 *     Sign       // 消息标志，请求／响应／通知，byte类型
 *     Type       // 消息类型，登录／发送消息等，byte类型
 *     Status     // 响应状态，成功／失败，byte类型
 *     BodyLength // 协议体长度，int类型
 *
 *
 * @author Yohann.
 */
public class ProtocolDecoder extends ByteToMessageDecoder {
    private static final Logger logger = Logger.getLogger(ProtocolDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < ProtocolHeader.HEADER_LENGTH) {
            // 数据包长度小于协议头长度
            logger.info("数据包长度小于协议头长度");
            ReferenceCountUtil.release(in);
            return;
        }
        if (in.readShort() != ProtocolHeader.MAGIC) {
            // Magic不一致，表明不是自己的数据
            logger.info("Magic不一致");
            ReferenceCountUtil.release(in);
            return;
        }

        // 开始解码
        byte sign = in.readByte();
        byte type = in.readByte();
        byte status = in.readByte();

        // 确认消息体长度
        int bodyLength = in.readInt();
        if (in.readableBytes() != bodyLength) {
            // 消息体长度不一致
            logger.info("消息体长度不一致");
            ReferenceCountUtil.release(in);
            return;
        }

        byte[] bytes = new byte[bodyLength];
        in.readBytes(bytes);

        MessageHolder messageHolder = new MessageHolder();
        messageHolder.setSign(sign);
        messageHolder.setType(type);
        messageHolder.setStatus(status);
        messageHolder.setBody(new String(bytes, "utf-8"));

        out.add(messageHolder);
    }
}
