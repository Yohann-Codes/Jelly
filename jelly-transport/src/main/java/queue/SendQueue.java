package queue;

import protocol.MessageHolder;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 发送阻塞队列，缓存即将出站的任务.
 *
 * Service Module ---> OutboundQueue ---> Transport Module.
 *
 * @author Yohann.
 */
public class SendQueue {
    private volatile static BlockingQueue<MessageHolder> queue;

    public static BlockingQueue<MessageHolder> getQueue() {
        if (queue == null) {
            synchronized (SendQueue.class) {
                if (queue == null) {
                    queue = new LinkedBlockingDeque<>(1024);
                }
            }
        }
        return queue;
    }
}
