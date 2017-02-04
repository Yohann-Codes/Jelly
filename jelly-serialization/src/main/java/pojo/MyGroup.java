package pojo;

import java.util.List;
import java.util.Map;

/**
 * @author Yohann.
 */
public class MyGroup {
    private String username;
    private Map<String, List<String>> groups;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Map<String, List<String>> getGroups() {
        return groups;
    }

    public void setGroups(Map<String, List<String>> groups) {
        this.groups = groups;
    }
}
