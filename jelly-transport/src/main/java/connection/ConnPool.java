package connection;

import io.netty.channel.Channel;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 连接池，用户维护已与服务器建立连接的Client
 * <p>
 * @author Yohann.
 */
public class ConnPool {
    private static final Logger logger = Logger.getLogger(ConnPool.class);

    private ConnPool() {
    }

    // 用于存放在线用户的username和channel
    private static Map<String, Channel> connsMap =
            new HashMap<>();

    /**
     * 添加连接
     *
     * @param username
     * @param channel
     * @return
     */
    public synchronized static boolean add(String username, Channel channel) {
        Channel result = connsMap.put(username, channel);
        if (result == null) {
            logger.info("Conn池 添加成功(username=" + username + " channel=" + channel + ")");
            return true;
        } else {
            logger.warn("Conn池 添加失败(username=" + username + " channel=" + channel + ")");
            return false;
        }
    }

    /**
     * 删除连接
     *
     * @param username
     * @return
     */
    public synchronized static boolean remove(String username) {
        Channel result = connsMap.remove(username);
        if (result != null) {
            logger.info("Conn池 移除成功(username=" + username + ")");
            return true;
        } else {
            logger.warn("Conn池 移除失败(username=" + username + ")");
            return false;
        }
    }

    /**
     * 查找连接
     *
     * @param username
     * @return
     */
    public synchronized static Channel query(String username) {
        return connsMap.get(username);
    }

    /**
     * 查找用户
     *
     * @param channel
     * @return
     */
    public synchronized static String query(Channel channel) {
        Set<Map.Entry<String, Channel>> entries = connsMap.entrySet();
        Iterator<Map.Entry<String, Channel>> ite = entries.iterator();
        while (ite.hasNext()) {
            Map.Entry<String, Channel> entry = ite.next();
            if (channel.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }
}
