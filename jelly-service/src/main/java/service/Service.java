package service;

import org.apache.log4j.Logger;
import protocol.MessageHolder;
import queue.TaskQueue;

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

    // 任务队列
    private BlockingQueue<MessageHolder> taskQueue;
    // 阻塞式地从taskQueue取MessageHolder
    private ExecutorService takeExecutor;
    // 执行业务的线程池
    private ExecutorService taskExecutor;

    public void initAndStart() {
        init();
        start();
    }

    private void init() {
        takeExecutor = Executors.newSingleThreadExecutor();
        taskExecutor = Executors.newFixedThreadPool(10);
        taskQueue = TaskQueue.getQueue();
        logger.info("初始化服务完成");
    }

    private void start() {
        takeExecutor.execute(new Runnable() {
            @Override
            public void run() {
                while (!shutdown.get()) {
                    try {
                        MessageHolder messageHolder = taskQueue.take();
                        logger.info("TaskQueue取出任务: taskQueue=" + taskQueue.size());
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
                        logger.info("开始执行取出的任务 messageHolder=" + messageHolder);
                        Dispatcher.dispatch(messageHolder);
                    }
                });
            }
        });
        logger.info("启动服务完成");
    }
}
