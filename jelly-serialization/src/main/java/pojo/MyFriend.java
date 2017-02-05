package pojo;

import java.util.Map;

/**
 * @author Yohann.
 */
public class MyFriend {
    private String username;
    // key->好友用户名，value->是否在线
    private Map<String, Boolean> friends;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Map<String, Boolean> getFriends() {
        return friends;
    }

    public void setFriends(Map<String, Boolean> friends) {
        this.friends = friends;
    }
}
