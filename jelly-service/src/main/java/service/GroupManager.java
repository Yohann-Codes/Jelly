package service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用于管理内存中的讨论组
 *
 * @author Yohann.
 */
public class GroupManager {

    /**
     * 存放从数据库调入的讨论组
     */
    private static Map<String, List<String>> groups = new HashMap<String, List<String>>();

    /**
     * 记录各讨论组最后一次活跃时间戳
     */
    private static Map<String, Long> groupTimes = new HashMap<String, Long>();

    public synchronized static void groupsAdd(String groupName, List<String> members) {
        groups.put(groupName, members);
    }

    public synchronized static void groupRemove(String groupName) {
        groups.remove(groupName);
    }

    public synchronized static List<String> groupsQuery(String groupName) {
        return  groups.get(groupName);
    }

    public static Map<String, List<String>> getGroups() {
        return groups;
    }

    public synchronized static void groupTimesAdd(String groupName, Long time) {
        groupTimes.put(groupName, time);
    }

    public synchronized static void groupTimesRemove(String groupName) {
        groupTimes.remove(groupName);
    }

    public synchronized static void groupTimesUpdate(String groupName, Long time) {
        groupTimes.put(groupName, time);
    }

    public synchronized static Set<Map.Entry<String, Long>> getGroupTimesIte() {
        Set<Map.Entry<String, Long>> entries = groupTimes.entrySet();
        return entries;
    }

    public static Map<String, Long> getGroupTimes() {
        return groupTimes;
    }
}
