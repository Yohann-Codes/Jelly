package protocol;

/**
 * 消息载体工厂.
 *
 * @author Yohann.
 */
public class MessageHolderFactory {

    public MessageHolderFactory() {}

    /**
     * 创建MessageHolder
     *
     * @param sign
     * @param type
     * @param status
     * @param object
     * @param bytes
     * @return
     */
    public MessageHolder newMessageHolder(byte sign, byte type,
                                          byte status, Object object, byte[] bytes) {
        MessageHolder messageHolder = new MessageHolder();
        messageHolder.setSign(sign);
        messageHolder.setType(type);
        messageHolder.setStatus(status);
        messageHolder.setObject(object);
        messageHolder.setBytes(bytes);
        return messageHolder;
    }
}
