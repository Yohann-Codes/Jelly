package handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.log4j.Logger;
import protocol.MessageHolder;
import queue.ReceiveQueue;

import java.util.concurrent.BlockingQueue;

/**
 * 最终接收数据的Handler，将待处理数据放入阻塞队列中，由服务模块take and deal.
 *
 * @author Yohann.
 */
public class AcceptorHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = Logger.getLogger(AcceptorHandler.class);

    private final BlockingQueue<MessageHolder> receiveQueue;

    public AcceptorHandler() {
        receiveQueue = ReceiveQueue.getQueue();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof MessageHolder) {
            MessageHolder messageHolder = (MessageHolder) msg;
            messageHolder.setChannel(ctx.channel());
            boolean offer = receiveQueue.offer(messageHolder);
            if (!offer) {
                // 服务器繁忙
                logger.warn("服务器繁忙");
            }
        } else {
            throw new IllegalArgumentException("msg is not instance of MessageHolder");
        }
    }
}
