package top.grayson.provider.impl;

import lombok.extern.slf4j.Slf4j;
import top.grayson.config.RpcServiceConfig;
import top.grayson.enums.RpcErrorMessageEnum;
import top.grayson.exception.RpcException;
import top.grayson.extension.ExtensionLoader;
import top.grayson.provider.ServiceProvider;
import top.grayson.registry.ServiceRegistry;
import top.grayson.remoting.constant.RpcConstants;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/19 15:27
 * @Description Zookeeper 服务提供者实现类
 */
@Slf4j
public class ZkServiceProviderImpl implements ServiceProvider {
    private final Map<String, Object> serviceMap;
    private final Set<String> registeredService;
    private final ServiceRegistry serviceRegistry;

    public ZkServiceProviderImpl() {
        this.serviceMap = new ConcurrentHashMap<>();
        this.registeredService = ConcurrentHashMap.newKeySet();
        this.serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension("zk");
    }

    /**
     * 添加 RPC 服务
     * @param rpcServiceConfig  RPC 服务配置信息
     */
    @Override
    public void addService(RpcServiceConfig rpcServiceConfig) {
        String rpcServiceName = rpcServiceConfig.getRpcServiceName();
        if (registeredService.contains(rpcServiceName)) {
            return;
        }
        registeredService.add(rpcServiceName);
        serviceMap.put(rpcServiceName, rpcServiceConfig.getService());
        log.info("Add service: {} and interfaces: {}", rpcServiceName, rpcServiceConfig.getService().getClass().getInterfaces());
    }

    /**
     * 获取 RPC 服务
     * @param rpcServiceName    RPC 服务名称
     * @return  指定名称的 RPC 服务
     */
    @Override
    public Object getService(String rpcServiceName) {
        Object service = serviceMap.get(rpcServiceName);
        if (null == service) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }

    /**
     * 发布 RPC 服务
     * @param rpcServiceConfig  RPC 服务配置信息
     */
    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            addService(rpcServiceConfig);
            serviceRegistry.registerService(rpcServiceConfig.getRpcServiceName(), new InetSocketAddress(host, RpcConstants.SERVER_PORT));
        } catch (UnknownHostException e) {
            log.error("Occur exception when get host address.", e);
        }
    }
}
