import handler.AcceptorHandler;
import handler.Handler;
import handler.ProtocolDecoder;
import handler.ProtocolEncoder;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import netty.NettyConfig;
import netty.NettyConfigImpl;

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
        NettyConfig config = new NettyConfigImpl();
        config.setParentGroup(1);
        config.setChildGroup();
        config.setChannel(NioServerSocketChannel.class);
        config.setHandler(handlers());
        config.bind(20000);
    }

    public static Handler[] handlers() {

        Handler decoderHandler = new Handler();
        decoderHandler.setName("ProtocolDecoder");
        decoderHandler.setHandler(new ProtocolDecoder());

        Handler encoderHandler = new Handler();
        encoderHandler.setName("ProtocolEncoder");
        encoderHandler.setHandler(new ProtocolEncoder());

        Handler acceptorHandler = new Handler();
        acceptorHandler.setName("AcceptorHandler");
        acceptorHandler.setHandler(new AcceptorHandler());

        return new Handler[]{decoderHandler, encoderHandler, acceptorHandler};
    }
}
