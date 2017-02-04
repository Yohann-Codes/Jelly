package operation;

import connect.JdbcConn;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 好友数据访问类
 * <p>
 * @author Yohann.
 */
public class FriendDao extends JdbcConn {

    /**
     * 在好友表中添加账户
     *
     * @param username
     * @return
     */
     public int insertAccount(String username) {
        String sql = "INSERT INTO friends (username) VALUES (?)";
        int row = 0;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.warn("MySQL添加账户出现异常", e);
        }
        return row;
    }

    /**
     * 添加好友
     *
     * @param username
     * @param friend
     * @param column
     * @return
     */
    public int insertFriend(String username, String friend, String column) {
        String sql = "update friends set " + column + " = ? where username = ?";
        int row = 0;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, friend);
            pstmt.setString(2, username);
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.warn("MySQL添加好友出现异常", e);
        }
        return row;
    }

    /**
     * 删除好友
     *
     * @param username
     * @return
     */
    public int removeFriend(String username, String column) {
        String sql = "update friends set " + column + " = ? where username = ?";
        int row = 0;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, null);
            pstmt.setString(2, username);
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.warn("MySQL删除好友出现异常", e);
        }
        return row;
    }

    /**
     * 查询所有好友
     *
     * @param username
     * @return
     */
    public List<String> queryAllFri(String username) {
        List<String> friends = new ArrayList<String>();
        String sql = "SELECT * FROM friends WHERE username = ?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                for (int i = 1; i <= 20; i++) {
                    String friend = resultSet.getString("friend_" + i);
                    if (friend != null) {
                        friends.add(friend);
                    }
                }
            }
        } catch (SQLException e) {
            logger.warn("MySQL查询所有好友出现异常", e);
        }
        return friends;
    }

    /**
     * 查询好友空列
     *
     * @param username
     * @return 列名
     */
    public String queryNoFriColumn(String username) {
        String sql = "SELECT * FROM friends WHERE username = ?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                for (int i = 1; i <= 20; i++) {
                    if (resultSet.getString("friend_" + i) == null) {
                        return "friend_" + i;
                    }
                }
            }
        } catch (SQLException e) {
            logger.warn("MySQL查询好友空位出现异常", e);
        }
        return null;
    }

    /**
     * 查询指定好友所在列名
     *
     * @param username
     * @param friend
     * @return
     */
    public String queryColumnByFri(String username, String friend) {
        String sql = "SELECT * FROM friends WHERE username = ?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                for (int i = 1; i <= 20; i++) {
                    if (friend.equals(resultSet.getString("friend_" + i))) {
                        return "friend_" + i;
                    }
                }
            }
        } catch (SQLException e) {
            logger.warn("MySQL查询好友所在列名出现异常", e);
        }
        return null;
    }
}
