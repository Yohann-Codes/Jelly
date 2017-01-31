package queue;

import protocol.MessageHolder;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 接收阻塞队列，缓存刚入站的任务.
 *
 * Transport Module ---> InboundQueue ---> Service Module.
 *
 * @author Yohann.
 */
public class ReceiveQueue {
    private volatile static BlockingQueue<MessageHolder> queue;

    public static BlockingQueue<MessageHolder> getQueue() {
        if (queue == null) {
            synchronized (ReceiveQueue.class) {
                if (queue == null) {
                    queue = new LinkedBlockingDeque<>(1024);
                }
            }
        }
        return queue;
    }
}
