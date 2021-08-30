package top.grayson.remoting.constant;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/18 17:31
 * @Description RPC 常量类
 */
public class RpcConstants {
    /**
     * 魔法数
     */
    public static final byte[] MAGIC_NUMBER = {(byte) 'g', (byte) 'r', (byte) 'p', (byte) 'c'};
    /**
     * 默认编码方式
     */
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    /**
     * 版本信息
     */
    public static final byte VERSION = 1;
    /**
     * 总长度
     */
    public static final byte TOTAL_LENGTH = 16;
    /**
     * 请求类型
     */
    public static final byte REQUEST_TYPE = 1;
    /**
     * 响应类型
     */
    public static final byte RESPONSE_TYPE = 2;
    /**
     * 心跳请求类型
     */
    public static final byte HEART_BEAT_REQUEST_TYPE = 3;
    /**
     * 心跳响应类型
     */
    public static final byte HEART_BEAT_RESPONSE_TYPE = 4;
    /**
     * 头长度
     */
    public static final byte HEAD_LENGTH = 16;
    /**
     * ping
     */
    public static final String PING = "ping";
    /**
     * pong
     */
    public static final String PONG = "pong";
    /**
     * 最大帧长
     */
    public static final int MAX_FRAME_LENGTH = 8 * 1024 * 1024;

    /**
     * 服务器端口
     */
    public static final int SERVER_PORT = 9998;
}
