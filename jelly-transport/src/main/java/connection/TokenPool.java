package connection;

import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * 维护token
 * <p>
 * @author Yohann.
 */
public class TokenPool {
    private static final Logger logger = Logger.getLogger(TokenPool.class);

    // 用于存放已生成的token
    private static Set<Long> tokenSet = new HashSet<>();

    private TokenPool() {
    }

    /**
     * 添加token
     *
     * @param token
     * @return
     */
    public synchronized static boolean add(Long token) {
        if (tokenSet.add(token)) {
            logger.info("Token池 添加成功(token=" + token);
            return true;
        }
        logger.warn("Token池 添加失败(token=" + token);
        return false;
    }

    /**
     * 删除token
     *
     * @param token
     * @return
     */
    public synchronized static boolean remove(Long token) {
        if (tokenSet.remove(token)) {
            logger.info("Token池 移除成功(token=" + token);
            return true;
        }
        logger.warn("Token池 移除失败(token=" + token);
        return false;
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
