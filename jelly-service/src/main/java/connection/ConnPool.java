package connection;

import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 连接池，用户维护已与服务器建立连接的Client
 * <p>
 * Created by yohann on 2017/1/10.
 */
public class ConnPool {

    private ConnPool() {
    }

    // 用于存放在线用户的username和channel
    private static Map<String, Channel> connsMap =
            new HashMap<String, Channel>();

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
            return true;
        } else {
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
            return true;
        } else {
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
