package connect;

/**
 * The interface of Database;
 *
 * @author Yohann.
 */
public interface Connect {

    /**
     * 连接MySQL数据库
     */
    void connect();

    /**
     * 关闭数据库资源
     */
    void close();
}
