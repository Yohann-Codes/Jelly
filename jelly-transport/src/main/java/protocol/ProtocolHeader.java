package protocol;

/**
 * Jelly传输层协议头.
 *
 *                                       Jelly Protocol
 *  __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __ __
 * |           |           |           |           |              |                          |
 *       2           1           1           1            4               Uncertainty
 * |__ __ __ __|__ __ __ __|__ __ __ __|__ __ __ __|__ __ __ __ __|__ __ __ __ __ __ __ __ __|
 * |           |           |           |           |              |                          |
 *     Magic        Sign        Type       Status     Body Length         Body Content
 * |__ __ __ __|__ __ __ __|__ __ __ __|__ __ __ __|__ __ __ __ __|__ __ __ __ __ __ __ __ __|
 *
 * 协议头9个字节定长
 *     Magic      // 数据包的验证位，short类型
 *     Sign       // 消息标志，请求／响应／通知，byte类型
 *     Type       // 消息类型，登录／发送消息等，byte类型
 *     Status     // 响应状态，成功／失败，byte类型
 *     BodyLength // 协议体长度，int类型
 *
 *
 * @author Yohann.
 */
public class ProtocolHeader {

    /** 协议头长度 */
    public static final int HEADER_LENGTH = 9;
    /** Magic */
    public static final short MAGIC = (short) 0xabcd;

    /** 消息标志 */
    private byte sign;

    /** sign: 0x01 ~ 0x03 =========================================================================================== */
    public static final byte REQUEST               = 0x01;    // 请求  Client --> Server
    public static final byte RESPONSE              = 0x02;    // 响应  Server --> Client
    public static final byte NOTICE                = 0x03;    // 通知  Server --> Client  e.g.消息转发

    /** 消息类型 */
    private byte type;

    /** type: 0x11 ~ 0x1f =========================================================================================== */
    public static final byte LOGIN                 = 0x11;    // 登录
    public static final byte REGISTER              = 0x12;    // 注册
    public static final byte LOGOUT                = 0x13;    // 登出
    public static final byte RECONN                = 0x14;    // 重连
    public static final byte PERSON_MESSAGE        = 0x15;    // 发送个人消息
    public static final byte GROUP_MESSAGE         = 0x16;    // 发送讨论组消息
    public static final byte CREATE_GROUP          = 0x17;    // 创建讨论组
    public static final byte DISBAND_GROUP         = 0x18;    // 解散讨论组
    public static final byte ADD_MEMBER            = 0x19;    // 讨论组添加成员
    public static final byte REMOVE_MEMBER         = 0x1a;    // 讨论组移除成员
    public static final byte UPDATE_SELF_INFO      = 0x1b;    // 修改个人信息
    public static final byte LOOK_SELF_INFO        = 0x1c;    // 查看个人信息
    public static final byte LOOK_OTHER_INFO       = 0x1d;    // 查看他人信息
    public static final byte LOOK_GROUP_INFO       = 0x1e;    // 查看自己所在讨论组信息
    public static final byte HEARTBEAT             = 0x1f;    // 心跳

    /** 响应状态 */
    private byte status;

    /** status: 0x1a ~ 0x1c ========================================================================================= */
    public static final byte SUCCESS               = 0x2a;    // 请求成功
    public static final byte REQUEST_FAILURE       = 0x2b;    // 请求错误
    public static final byte SERVER_BUSY           = 0x2c;    // 服务器繁忙
    public static final byte SERVER_FAILURE        = 0x2d;    // 服务器错误

    /** 消息体长度 */
    private int bodyLength;
}
