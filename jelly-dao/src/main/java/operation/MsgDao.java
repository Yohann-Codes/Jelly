package operation;

import connect.JdbcConn;
import pojo.OfflineMessage;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * 离线消息访问类
 * <p>
 * @author Yohann.
 */
public class MsgDao extends JdbcConn {

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
            logger.warn("MySQL添加离线消息出现异常", e);
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
            logger.warn("MySQL删除离线消息出现异常", e);
        }
        return row;
    }

    /**
     * 查询离线消息
     *
     * @param receiver
     * @return
     */
    public List<OfflineMessage> queryMsg(String receiver) {
        List<OfflineMessage> offlineMsgs = new ArrayList<OfflineMessage>();
        String sql = "SELECT * FROM offline_msg WHERE receiver = ?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, receiver);
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                OfflineMessage offlineMsg = new OfflineMessage();
                offlineMsg.setSender(resultSet.getString("sender"));
                offlineMsg.setReceiver(resultSet.getString("receiver"));
                offlineMsg.setMessage(resultSet.getString("message"));
                offlineMsg.setTime(resultSet.getTimestamp("time").getTime());
                offlineMsgs.add(offlineMsg);
            }
        } catch (SQLException e) {
            logger.warn("MySQL查询离线消息出现异常", e);
        }
        return offlineMsgs;
    }
}
