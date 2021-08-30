package top.grayson.enums;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/18 15:38
 * @Description RPC 响应编码枚举类
 */
@AllArgsConstructor
@Getter
@ToString
public enum RpcResponseCodeEnum {
    SUCCESS(HttpResponseStatus.OK.code(), "The remote call is successful."),
    FAIL(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), "The remote call is fail.");

    private final int code;
    private final String message;
}
