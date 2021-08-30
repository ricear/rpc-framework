package top.grayson.provider;

import top.grayson.config.RpcServiceConfig;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/19 15:16
 * @Description 存储并且提供服务对象
 */
public interface ServiceProvider {
    /**
     * 添加 RPC 服务
     * @param rpcServiceConfig  RPC 服务配置信息
     */
    void addService(RpcServiceConfig rpcServiceConfig);

    /**
     * 获取 RPC 服务
     * @param rpcServiceName    RPC 服务名称
     * @return  RPC 服务
     */
    Object getService(String rpcServiceName);

    /**
     * 发布服务
     * @param rpcServiceConfig  RPC 服务配置信息
     */
    void publishService(RpcServiceConfig rpcServiceConfig);
}
