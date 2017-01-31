package netty;

import exception.NullParamsException;
import handler.Handler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import org.apache.log4j.Logger;

/**
 * The implementation of NettyConfig.
 *
 * @author Yohann.
 */
public class NettyConfigImpl implements NettyConfig {
    private static final Logger logger = Logger.getLogger(NettyConfigImpl.class);

    private final ServerBootstrap bootstrap;
    private EventLoopGroup parentGroup;
    private EventLoopGroup childGroup;
    private Class channelClass;

    public NettyConfigImpl() {
        bootstrap = new ServerBootstrap();
    }

    @Override
    public void setParentGroup() {
        parentGroup = new NioEventLoopGroup();
    }

    @Override
    public void setParentGroup(int nThreads) {
        parentGroup = new NioEventLoopGroup(nThreads);
    }

    @Override
    public void setChildGroup() {
        childGroup = new NioEventLoopGroup();
    }

    @Override
    public void setChildGroup(int nThreads) {
        childGroup = new NioEventLoopGroup(nThreads);
    }

    @Override
    public void setChannel(Class channelClass) {
        this.channelClass = channelClass;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setHandler(Handler[] handlers) {
        validate();
        bootstrap.group(parentGroup, childGroup);
        bootstrap.channel(channelClass);
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                for (Handler handler : handlers) {
                    pipeline.addLast(handler.getName(), handler.getHandler());
                }
            }
        });
    }

    @Override
    public void bind(int port) {
        bind(port, true);
    }

    @Override
    public void bind(int port, boolean sync) {
        ChannelFuture future = null;

        try {
            future = bootstrap.bind(port).sync();
            logger.info("服务器启动成功 port=" + port);

            if (sync) {
                future.channel().closeFuture().sync();
            } else {
                future.channel().closeFuture();
            }
            logger.info("服务器关闭");

        } catch (InterruptedException e) {
            logger.warn("Netty绑定异常", e);
        } finally {
            parentGroup.shutdownGracefully();
            childGroup.shutdownGracefully();
        }
    }

    private void validate() {
        if (parentGroup == null
                || childGroup == null
                || channelClass == null) {
            throw new NullParamsException("parentGroup == null " +
                    "|| childGroup == null " +
                    "|| channelClass == null");
        }
    }
}
