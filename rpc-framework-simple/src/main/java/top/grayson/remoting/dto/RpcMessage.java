package top.grayson.remoting.dto;

import lombok.*;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/18 17:25
 * @Description
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcMessage {
    /**
     * 消息类型
     */
    private byte messageType;
    /**
     * 序列化类型
     */
    private byte codec;
    /**
     * 压缩类型
     */
    private byte compress;
    /**
     * 请求 ID
     */
    private int requestId;
    /**
     * 响应数据
     */
    private Object data;

}
