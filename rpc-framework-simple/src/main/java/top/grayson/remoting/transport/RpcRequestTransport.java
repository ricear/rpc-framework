package top.grayson.remoting.transport;

import top.grayson.extension.SPI;
import top.grayson.remoting.dto.RpcRequest;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/18 16:32
 * @Description 发送 RPC 请求
 */
@SPI
public interface RpcRequestTransport {
    /**
     * 发送 RPC 请求到服务器，并获取服务器的响应结果
     * @param rpcRequest    RPC 请求
     * @return  服务器响应结果
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}
