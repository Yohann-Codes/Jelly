package connect;

import org.apache.log4j.Logger;

import java.sql.*;
import java.util.Properties;

/**
 * 通过JDBC连接数据库.
 *
 * @author Yohann.
 */
public class JdbcConn implements Connect {

    public static final Logger logger = Logger.getLogger(JdbcConn.class);

    //需要关闭的资源
    protected Connection conn;
    protected PreparedStatement pstmt;
    protected ResultSet resultSet;

    @Override
    public void connect() {
        //加载JDBC驱动
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            logger.warn("JDBC Driver", e);
        }

        //准备数据库连接数据
        Properties info = new Properties();
        String url = JdbcConfig.DB_URL;
        info.put("user", JdbcConfig.DB_USERNAME);
        info.put("password", JdbcConfig.DB_PASSWORD);

        //获取连接对象
        try {
            conn = DriverManager.getConnection(url, info);
        } catch (SQLException e) {
            logger.warn("DriverManager.getConnection", e);
        }
    }

    @Override
    public void close() {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                logger.warn("MySQL关闭ResultSet出现异常", e);
            }
        }
        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (SQLException e) {
                logger.warn("MySQL关闭PreparedStatement出现异常", e);
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.warn("MySQL关闭Connection出现异常", e);
            }
        }
    }
}
