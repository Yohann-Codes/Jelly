package bean;

/**
 * 离线消息对象模型
 * <p>
 * Created by yohann on 2017/1/16.
 */
public class OfflineMsgBean {
    private String sender;
    private String receiver;
    private String message;
    private long time;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
