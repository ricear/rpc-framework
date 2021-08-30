package top.grayson.registry;

import top.grayson.extension.SPI;
import top.grayson.remoting.dto.RpcRequest;

import java.net.InetSocketAddress;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/18 16:36
 * @Description 服务发现
 */
@SPI
public interface ServiceDiscovery {
    /**
     * 根据 服务名称 查询 服务
     * @param rpcRequest
     * @return
     */
    InetSocketAddress lookupService(RpcRequest rpcRequest);
}
