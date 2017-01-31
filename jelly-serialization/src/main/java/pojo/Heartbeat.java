package pojo;

/**
 * @author Yohann.
 */
public class Heartbeat {
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "Heartbeat{" +
                "username='" + username + '\'' +
                '}';
    }
}
