package pojo;

/**
 * @author Yohann.
 */
public class Member {
    // ADD_MEMBER, REMOVE_MEMBER
    private byte type;
    private String username;
    private String groupName;
    private String member;

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

    public String getMember() {
        return member;
    }

    public void setMember(String member) {
        this.member = member;
    }

    @Override
    public String toString() {
        return "Member{" +
                "type=" + type +
                ", username='" + username + '\'' +
                ", groupName='" + groupName + '\'' +
                ", member='" + member + '\'' +
                '}';
    }
}
