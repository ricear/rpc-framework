package top.grayson.registry;

import top.grayson.extension.SPI;

import java.net.InetSocketAddress;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/19 15:30
 * @Description 服务注册
 */
@SPI
public interface ServiceRegistry {
    /**
     * 注册服务
     * @param rpcServiceName    RPC 服务名称
     * @param inetSocketAddress 服务器地址
     */
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);
}
