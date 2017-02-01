package dao;

import bean.OfflineMsgBean;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * 离线消息访问类
 * <p>
 * Created by yohann on 2017/1/16.
 */
public class OfflineMsgDao extends Dao {

    /**
     * 连接MySQL数据库
     *
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public OfflineMsgDao() throws ClassNotFoundException, SQLException {
        super();
    }

    /**
     * 添加离线消息
     *
     * @param sender
     * @param receiver
     * @param msg
     * @param time
     * @return
     */
    public int insertMsg(String sender, String receiver, String msg, Timestamp time) {
        String sql = "INSERT INTO offline_msg (sender, receiver, message, time) VALUES (?, ?, ?, ?)";
        int row = 0;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, sender);
            pstmt.setString(2, receiver);
            pstmt.setString(3, msg);
            pstmt.setTimestamp(4, time);
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.warn("MySQL添加离线消息出现异常", e);
        }
        return row;
    }

    /**
     * 删除离线消息
     *
     * @param receiver
     * @return
     */
    public int removeMsg(String receiver) {
        String sql = "DELETE FROM offline_msg where receiver = ?";
        int row = 0;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, receiver);
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.warn("MySQL删除离线消息出现异常", e);
        }
        return row;
    }

    /**
     * 查询离线消息
     *
     * @param receiver
     * @return
     */
    public List<OfflineMsgBean> queryMsg(String receiver) {
        List<OfflineMsgBean> offlineMsgs = new ArrayList<OfflineMsgBean>();
        String sql = "SELECT * FROM offline_msg WHERE receiver = ?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, receiver);
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                OfflineMsgBean offlineMsg = new OfflineMsgBean();
                offlineMsg.setSender(resultSet.getString("sender"));
                offlineMsg.setReceiver(resultSet.getString("receiver"));
                offlineMsg.setMessage(resultSet.getString("message"));
                offlineMsg.setTime(resultSet.getTimestamp("time").getTime());
                offlineMsgs.add(offlineMsg);
            }
        } catch (SQLException e) {
            LOGGER.warn("MySQL查询离线消息出现异常", e);
        }
        return offlineMsgs;
    }
}
