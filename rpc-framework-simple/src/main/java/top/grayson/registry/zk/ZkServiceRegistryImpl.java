package top.grayson.registry.zk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import top.grayson.registry.ServiceRegistry;
import top.grayson.registry.zk.util.CuratorUtils;

import java.net.InetSocketAddress;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/20 09:59
 * @Description
 */
@Slf4j
public class ZkServiceRegistryImpl implements ServiceRegistry {
    /**
     * 注册服务
     *
     * @param rpcServiceName    RPC 服务名称
     * @param inetSocketAddress 服务器地址
     */
    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        CuratorUtils.createPersistentNode(zkClient, servicePath);
    }
}
