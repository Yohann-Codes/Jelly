package pojo;

import java.util.List;

/**
 * @author Yohann.
 */
public class MyFriend {
    private String username;
    private List<String> friends;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getFriends() {
        return friends;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends;
    }
}
