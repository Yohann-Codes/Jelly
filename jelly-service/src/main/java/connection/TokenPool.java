package connection;

import java.util.HashSet;
import java.util.Set;

/**
 * 维护token
 * <p>
 * Created by yohann on 2017/1/15.
 */
public class TokenPool {

    // 用于存放已生成的token
    private static Set<Long> tokenSet = new HashSet<Long>();

    private TokenPool() {
    }

    /**
     * 添加token
     *
     * @param token
     * @return
     */
    public synchronized static boolean add(Long token) {
        return tokenSet.add(token);
    }

    /**
     * 删除token
     *
     * @param token
     * @return
     */
    public synchronized static boolean remove(Long token) {
        return tokenSet.remove(token);
    }

    /**
     * 查询token是否存在
     *
     * @param token
     * @return
     */
    public synchronized static boolean query(Long token) {
        return tokenSet.contains(token);
    }
}
