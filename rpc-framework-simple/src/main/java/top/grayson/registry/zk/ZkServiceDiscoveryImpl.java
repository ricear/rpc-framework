package top.grayson.registry.zk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import top.grayson.enums.RpcErrorMessageEnum;
import top.grayson.exception.RpcException;
import top.grayson.extension.ExtensionLoader;
import top.grayson.loadbalance.LoadBalance;
import top.grayson.registry.ServiceDiscovery;
import top.grayson.registry.zk.util.CuratorUtils;
import top.grayson.remoting.dto.RpcRequest;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/19 16:21
 * @Description 基于 Zookeeper 实现的服务发现
 */
@Slf4j
public class ZkServiceDiscoveryImpl implements ServiceDiscovery {
    private final LoadBalance loadBalance;

    public ZkServiceDiscoveryImpl() {
        this.loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension("loadBalance");
    }

    /**
     * 查询服务
     * @param rpcRequest    RPC 请求
     * @return  目标服务地址
     */
    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        String rpcServiceName = rpcRequest.getRpcServiceName();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> serviceUrlList = CuratorUtils.getChildrenNodes(zkClient, rpcServiceName);
        if (serviceUrlList == null || serviceUrlList.size() == 0) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND, rpcServiceName);
        }
        //  负载均衡
        String targetServiceUrl = loadBalance.selectServiceAddress(serviceUrlList, rpcRequest);
        log.info("Successfully found the service address: [{}]", targetServiceUrl);
        String[] socketAddressArray = targetServiceUrl.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host, port);
    }
}
