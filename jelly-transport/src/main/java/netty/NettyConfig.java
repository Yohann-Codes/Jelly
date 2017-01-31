package netty;

import handler.Handler;

/**
 * Netty配置接口.
 *
 * @author Yohann.
 */
public interface NettyConfig {

    /**
     * 设置parent处理器线程数为默认值，the number of processors available * 2
     */
    void setParentGroup();

    /**
     * 指定parent处理器线程数
     *
     * @param nThreads 线程数量
     */
    void setParentGroup(int nThreads);

    /**
     * 设置child处理器线程数为默认值，the number of processors available * 2
     */
    void setChildGroup();

    /**
     * 指定child处理器线程数
     *
     * @param nThreads 线程数量
     */
    void setChildGroup(int nThreads);

    /**
     * 设置需要注册的channel
     *
     * @param channelClass channel类对象
     */
    void setChannel(Class channelClass);

    /**
     * 设置handler
     *
     * @param handlers 稳定的handler
     */
    void setHandler(Handler[] handlers);

    /**
     * 同步绑定端口
     *
     * @param port 绑定端口号
     */
    void bind(int port);

    /**
     * 绑定端口
     *
     * @param port 绑定端口号
     * @param sync 同步->true，异步->false
     */
    void bind(int port, boolean sync);
}
