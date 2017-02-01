package service;

import org.apache.log4j.Logger;
import protocol.MessageHolder;
import queue.ReceiveQueue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The implementation of Service.
 *
 * @author Yohann.
 */
public class Service {
    private static final Logger logger = Logger.getLogger(Service.class);

    public static AtomicBoolean shutdown = new AtomicBoolean(false);

    // 阻塞式地从ReceiveQueue取MessageHolder
    private ExecutorService takeExecutor;
    // 阻塞式地往SendQueue放MessageHolder
    private ExecutorService putExecutor;

    private BlockingQueue<MessageHolder> receiveQueue;
    private ExecutorService taskExecutor;

    public void initAndStart() {
        init();
        start();
    }

    private void init() {
        takeExecutor = Executors.newSingleThreadExecutor();
        putExecutor = Executors.newSingleThreadExecutor();
        taskExecutor = Executors.newFixedThreadPool(10);
        receiveQueue = ReceiveQueue.getQueue();
    }

    private void start() {
        takeExecutor.execute(new Runnable() {
            @Override
            public void run() {
                while (!shutdown.get()) {
                    try {
                        MessageHolder messageHolder = receiveQueue.take();
                        startTask(messageHolder);
                    } catch (InterruptedException e) {
                        logger.warn("receiveQueue take", e);
                    }
                }
            }

            private void startTask(MessageHolder messageHolder) {
                taskExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        Dispatcher.dispatch(messageHolder);
                    }
                });
            }
        });
    }
}
