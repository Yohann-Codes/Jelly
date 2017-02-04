import io.netty.channel.socket.nio.NioServerSocketChannel;
import netty.NettyConfig;
import netty.NettyConfigImpl;
import service.Service;

/**
 * 启动类
 *
 * @author Yohann.
 */
public class Launcher {
    public static void main(String[] args) throws InterruptedException {
        start();
    }

    public static void start() throws InterruptedException {

        // 启动服务
        new Service().initAndStart();

        NettyConfig config = new NettyConfigImpl();
        config.setParentGroup(1);
        config.setChildGroup();
        config.setChannel(NioServerSocketChannel.class);
        config.setHandler();
        config.bind(20000);
    }
}
