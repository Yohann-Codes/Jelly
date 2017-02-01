package dao;

import config.Config;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.Properties;

/**
 * 数据访问层
 * <p>
 * Created by yohann on 2017/1/8.
 */
public class Dao {
    public static final Logger LOGGER = Logger.getLogger(Dao.class);

    //需要关闭的资源
    protected Connection conn;
    protected PreparedStatement pstmt;
    protected ResultSet resultSet;

    /**
     * 连接MySQL数据库
     *
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public Dao() throws ClassNotFoundException, SQLException {
        //加载JDBC驱动
        Class.forName("com.mysql.jdbc.Driver");

        //准备数据库连接数据
        Properties info = new Properties();
        String url = Config.DB_URL;
        info.put("user", Config.DB_USERNAME);
        info.put("password", Config.DB_PASSWORD);

        //获取连接对象
        conn = DriverManager.getConnection(url, info);
    }

    /**
     * 关闭数据库资源
     */
    public void close() {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                LOGGER.warn("MySQL关闭ResultSet出现异常", e);
            }
        }
        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (SQLException e) {
                LOGGER.warn("MySQL关闭PreparedStatement出现异常", e);
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                LOGGER.warn("MySQL关闭Connection出现异常", e);
            }
        }
    }
}
