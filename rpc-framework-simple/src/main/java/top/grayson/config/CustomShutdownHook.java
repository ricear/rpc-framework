package top.grayson.config;

import lombok.extern.slf4j.Slf4j;
import top.grayson.registry.zk.util.CuratorUtils;
import top.grayson.remoting.constant.RpcConstants;
import top.grayson.util.concurrent.threadpool.ThreadPoolFactoryUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/19 16:06
 * @Description 服务停止后进行的一些处理（例如取消注册所有服务）
 */
@Slf4j
public class CustomShutdownHook {
    private static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();

    /**
     * 获取 CustomShutdownHook 对象
     * @return  CustomShutdownHook 对象
     */
    public static CustomShutdownHook getCustomShutdownHook() {
        return CUSTOM_SHUTDOWN_HOOK;
    }

    /**
     * 服务停止后进行相应处理：
     *  1. 取消注册所有服务
     *  2. 关闭所有线程池
     */
    public void clearAll() {
        log.info("Add shutdown hook for clear all.");
        try {
            InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLocalHost().getCanonicalHostName(), RpcConstants.SERVER_PORT);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        Runtime.getRuntime()
                .addShutdownHook(new Thread(() -> {
                    try {
                        InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), RpcConstants.SERVER_PORT);
                        CuratorUtils.clearRegistry(CuratorUtils.getZkClient(), inetSocketAddress);
                    } catch (UnknownHostException e) {
                        log.error("Get local host error.");
                    }
                    ThreadPoolFactoryUtils.shutDownAllThreadPool();
                }));
    }
}
