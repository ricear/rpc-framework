package top.grayson.remoting.dto;

import lombok.*;
import top.grayson.enums.RpcResponseCodeEnum;

import java.io.Serializable;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/18 15:29
 * @Description 服务端响应实体类
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcResponse<T> implements Serializable {
    private static final long serialVersionUID = -6629490320105949744L;

    private String requestId;
    /**
     * response code
     */
    private Integer code;
    /**
     * response message
     */
    private String message;
    /**
     * response body
     */
    private T data;

    /**
     * 响应成功
     * @param data  响应数据
     * @param requestId 请求 ID
     * @param <T>   数据类型
     * @return  响应信息
     */
    public static <T> RpcResponse<T> success(T data, String requestId) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(RpcResponseCodeEnum.SUCCESS.getCode());
        response.setMessage(RpcResponseCodeEnum.SUCCESS.getMessage());
        response.setRequestId(requestId);
        if (null != data) {
            response.setData(data);
        }
        return response;
    }

    /**
     * 响应失败
     * @param rpcResponseCodeEnum   响应类型
     * @param <T>   数据类型
     * @return  响应信息
     */
    public static <T> RpcResponse<T> fail(RpcResponseCodeEnum rpcResponseCodeEnum) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(rpcResponseCodeEnum.getCode());
        response.setMessage(rpcResponseCodeEnum.getMessage());
        return response;
    }
}
