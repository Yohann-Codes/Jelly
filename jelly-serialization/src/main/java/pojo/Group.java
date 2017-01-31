package pojo;

/**
 * @author Yohann.
 */
public class Group {
    // CREATE_GROUP, DISBAND_GROUP
    private byte type;
    private String username;
    private String groupName;

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public String toString() {
        return "Group{" +
                "type=" + type +
                ", username='" + username + '\'' +
                ", groupName='" + groupName + '\'' +
                '}';
    }
}
