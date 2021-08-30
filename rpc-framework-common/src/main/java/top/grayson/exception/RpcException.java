package top.grayson.exception;

import top.grayson.enums.RpcErrorMessageEnum;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/19 15:38
 * @Description RPC 异常类
 */
public class RpcException extends RuntimeException {
    public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum, String detail) {
        super(String.format("%s:%s", rpcErrorMessageEnum.getMessage(), detail));
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum) {
        super(rpcErrorMessageEnum.getMessage());
    }
}
