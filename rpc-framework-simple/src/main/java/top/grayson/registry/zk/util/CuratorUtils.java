package top.grayson.registry.zk.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import top.grayson.enums.RpcConfigEnum;
import top.grayson.util.PropertyFileUtils;

import javax.net.ssl.SSLEngineResult;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/19 16:14
 * @Description Curator 工具类
 */
@Slf4j
public class CuratorUtils {
    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RETRIES = 3;
    public static final String ZK_REGISTER_ROOT_PATH = "/my-rpc";
    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();
    private static CuratorFramework zkClient;
    private static final String DEFAULT_ZOOKEEPER_ADDRESS = "127.0.0.1:2181";

    /**
     * 获取 Zookeeper 客户端
     *
     * @return Zookeeper 客户端
     */
    public static CuratorFramework getZkClient() {
        //  检查用户是否设置了 zk 地址
        Properties properties = PropertyFileUtils.readPropertiesFile(RpcConfigEnum.RPC_CONFIG_PATH.getPropertyValue());
        String zookeeperAddress = (properties != null && properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue()) != null ? properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue()) : DEFAULT_ZOOKEEPER_ADDRESS);
        //  如果 zkClient 已经启动了，则直接返回
        if (zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED) {
            return zkClient;
        }
        //  重试策略：重试 3 次，而且每次重试都会增加睡眠时间
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        zkClient = CuratorFrameworkFactory.builder()
                //  要连接的 Zookeeper 服务，可以是一个服务列表
                .connectString(zookeeperAddress)
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();
        try {
            if (!zkClient.blockUntilConnected(30, TimeUnit.SECONDS)) {
                throw new RuntimeException("Timeout when waiting to connect to zk!");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return zkClient;
    }

    /**
     * 在一个节点下获取子节点
     *
     * @param zkClient       Zookeeper 客户端
     * @param rpcServiceName RPC 服务名称
     * @return 当前节点下的子节点
     */
    public static List<String> getChildrenNodes(CuratorFramework zkClient, String rpcServiceName) {
        if (SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)) {
            return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        }
        List<String> result = null;
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        try {
            result = zkClient.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName, result);
            registerWatcher(rpcServiceName, zkClient);
        } catch (Exception e) {
            log.error("Get children nodes for path [{}] failed.", e);
        }
        return result;
    }

    /**
     * 注册监视器，监听指定节点下的变化
     *
     * @param rpcServiceName RPC 服务名称
     * @param zkClient       Zookeeper 客户端
     * @throws Exception
     */
    private static void registerWatcher(String rpcServiceName, CuratorFramework zkClient) throws Exception {
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);
        pathChildrenCache.getListenable()
                .addListener(((curatorFramework, pathChildrenCacheEvent) -> {
                    List<String> serviceAddresses = curatorFramework.getChildren().forPath(servicePath);
                    SERVICE_ADDRESS_MAP.put(rpcServiceName, serviceAddresses);
                }));
        pathChildrenCache.start();
    }

    /**
     * 创建永久节点，和临时节点不同的是，永久节点在客户端断开连接时不会被移除
     *
     * @param zkClient Zookeeper 客户端
     * @param path     要创建永久节点的路径
     */
    public static void createPersistentNode(CuratorFramework zkClient, String path) {
        try {
            if (REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null) {
                log.info("The node [{}] already exists", path);
            } else {
                //eg: /my-rpc/github.javaguide.HelloService/127.0.0.1:9999
                zkClient.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(path);
                log.info("The node [{}] was created successfully.", path);
            }
        } catch (Exception e) {
            log.error("Created persistent for path [{}] failed.", path);
        }
    }

    /**
     * 清除所有注册的服务
     *
     * @param zkClient          Zookeeper 客户端
     * @param inetSocketAddress RPC 服务地址
     */
    public static void clearRegistry(CuratorFramework zkClient, InetSocketAddress inetSocketAddress) {
        REGISTERED_PATH_SET.stream()
                .parallel()
                .forEach((path -> {
                    try {
                        if (path.endsWith(inetSocketAddress.toString())) {
                            zkClient.delete().forPath(path);
                            log.info("Node path for [{}] was deleted successfully.", path);
                        }
                    } catch (Exception e) {
                        log.info("Node path for [{}] was deleted failed.", path);
                    }
                }));
        log.info("All registered services on the service are cleared.");
    }
}
