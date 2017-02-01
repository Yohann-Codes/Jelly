package connection;

import java.util.Random;

/**
 * 用户创建token
 * <p>
 * Created by yohann on 2017/1/15.
 */
public class TokenFactory {
    public TokenFactory() {
    }

    /**
     * 调用此方法生成一个token
     *
     * @return
     */
    public Long generate() {
        Long token = null;

        Random random = new Random();

        while (isExist(token = random.nextLong())) {
            token = random.nextLong();
        }

        return token;
    }

    private boolean isExist(Long token) {
        return TokenPool.query(token);
    }
}
