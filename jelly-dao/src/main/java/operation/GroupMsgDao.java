package operation;

import connect.JdbcConn;
import pojo.OfflineGroupMessage;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * 离线消息访问类
 * <p>
 * @author Yohann.
 */
public class GroupMsgDao extends JdbcConn {

    /**
     * 添加离线消息
     *
     * @param sender
     * @param name
     * @param msg
     * @param time
     * @return
     */
    public int insertMsg(String sender,String receiver, String name, String msg, Timestamp time) {
        String sql = "INSERT INTO offline_msg_group (sender,receiver, name, message, time) VALUES (?, ?, ?, ?, ?)";
        int row = 0;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, sender);
            pstmt.setString(2, receiver);
            pstmt.setString(3, name);
            pstmt.setString(4, msg);
            pstmt.setTimestamp(5, time);
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
        String sql = "DELETE FROM offline_msg_group where receiver = ?";
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
    public List<OfflineGroupMessage> queryMsg(String receiver) {
        List<OfflineGroupMessage> offlineMsgs = new ArrayList<OfflineGroupMessage>();
        String sql = "SELECT * FROM offline_msg_group WHERE receiver = ?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, receiver);
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                OfflineGroupMessage offlineMsg = new OfflineGroupMessage();
                offlineMsg.setSender(resultSet.getString("sender"));
                offlineMsg.setReceiver(resultSet.getString("receiver"));
                offlineMsg.setGroup(resultSet.getString("name"));
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
