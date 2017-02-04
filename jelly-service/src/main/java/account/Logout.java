package account;

import connection.ConnPool;
import connection.TokenPool;
import handler.HeartbeatHandler;
import io.netty.channel.Channel;
import org.apache.log4j.Logger;
import pojo.Account;

/**
 * 登出服务.
 *
 * @author Yohann.
 */
public class Logout {
    private static final Logger logger = Logger.getLogger(Logout.class);

    private Account account;
    private Channel channel;
    private String username;

    public Logout(Account account, Channel channel) {
        this.account = account;
        this.channel = channel;
        username = account.getUsername();
    }

    public void deal() {
        // 移除维护的连接和token
        TokenPool.remove(account.getToken());

        // 标记为登出状态
        HeartbeatHandler.isLogout.set(true);

        // 关闭channel
        try {
            channel.close().sync();
        } catch (InterruptedException e) {
            logger.warn("关闭channel异常", e);
        }
    }
}
