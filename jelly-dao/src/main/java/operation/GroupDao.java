package operation;

import connect.JdbcConn;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 讨论组数据访问接口
 * <p>
 * @author Yohann.
 */
public class GroupDao extends JdbcConn {

    /**
     * 添加一个讨论组
     *
     * @param groupName
     * @param creater
     * @return
     */
    public int insertGroup(String groupName, String creater) {
        String sql = "INSERT INTO groups (name, creater) VALUES (?, ?)";
        int row = 0;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, groupName);
            pstmt.setString(2, creater);
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.warn("MySQL添加讨论组出现异常", e);
        }
        return row;
    }

    /**
     * 删除讨论组
     *
     * @param groupName
     * @return
     */
    public int removeGroup(String groupName) {
        String sql = "DELETE FROM groups where name = ?";
        int row = 0;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, groupName);
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.warn("MySQL删除讨论组信息出现异常", e);
        }
        return row;
    }

    /**
     * 查询成员空列
     *
     * @param groupName
     * @return 列名
     */
    public String queryNoMemColumn(String groupName) {
        String sql = "SELECT * FROM groups WHERE name = ?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, groupName);
            resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                for (int i = 1; i <= 10; i++) {
                    if (resultSet.getString("member_" + i) == null) {
                        return "member_" + i;
                    }
                }
            }
        } catch (SQLException e) {
            logger.warn("MySQL查询讨论组成员空位出现异常", e);
        }
        return null;
    }

    /**
     * 添加成员
     *
     * @param groupName
     * @param member
     * @param column
     * @return
     */
    public int insertMember(String groupName, String member, String column) {
        String sql = "update groups set " + column + " = ? where name = ?";
        int row = 0;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, member);
            pstmt.setString(2, groupName);
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.warn("MySQL添加讨论组成员出现异常", e);
        }
        return row;
    }

    /**
     * 查询指定成员所在列名
     *
     * @param groupName
     * @param member
     * @return
     */
    public String queryColumnByMem(String groupName, String member) {
        String sql = "SELECT * FROM groups WHERE name = ?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, groupName);
            resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                for (int i = 1; i <= 10; i++) {
                    if (member.equals(resultSet.getString("member_" + i))) {
                        return "member_" + i;
                    }
                }
            }
        } catch (SQLException e) {
            logger.warn("MySQL查询成员所在列名出现异常", e);
        }
        return null;
    }

    /**
     * 删除成员
     *
     * @param groupName
     * @param column
     * @return
     */
    public int removeMember(String groupName, String column) {
        String sql = "update groups set " + column + " = ? where name = ?";
        int row = 0;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, null);
            pstmt.setString(2, groupName);
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.warn("MySQL删除讨论组成员出现异常", e);
        }
        return row;
    }

    /**
     * 根据讨论组名称查询成员
     *
     * @param groupName
     * @return 返回值为null，讨论组不存在
     */
    public List<String> queryMemberByGroupName(String groupName) {
        List<String> members = null;
        String sql = "SELECT * FROM groups WHERE name = ?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, groupName);
            resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                members = new ArrayList<String>();
                members.add(resultSet.getString("creater"));
                for (int i = 1; i <= 10; i++) {
                    String member = resultSet.getString("member_" + i);
                    if (member != null) {
                        members.add(member);
                    }
                }
            }
        } catch (SQLException e) {
            logger.warn("MySQL查询讨论组成员出现异常", e);
        }
        return members;
    }

    /**
     * 查询全部存在member的讨论组以及成员
     *
     * @param member
     * @return
     */
    public Map<String, List<String>> queryAllbyMember(String member) {
        Map<String, List<String>> groups = new HashMap<String, List<String>>();
        String sql = "SELECT * FROM groups";
        try {
            pstmt = conn.prepareStatement(sql);
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                boolean isExist = false;
                List<String> members = new ArrayList<String>();
                String creater = resultSet.getString("creater");
                members.add(creater);
                if (member.equals(creater)) {
                    isExist = true;
                }
                for (int i = 1; i <= 10; i++) {
                    String m = resultSet.getString("member_" + i);
                    if (m != null) {
                        members.add(m);
                    }
                    if (member.equals(m)) {
                        isExist = true;
                    }
                }
                if (isExist) {
                    groups.put(resultSet.getString("name"), members);
                }
            }
        } catch (SQLException e) {
            logger.warn("MySQL查询全部讨论组出现异常", e);
        }
        return groups;
    }
}
