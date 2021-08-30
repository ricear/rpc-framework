import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import top.grayson.HelloService;
import top.grayson.annotation.RpcScan;
import top.grayson.config.RpcServiceConfig;
import top.grayson.remoting.transport.netty.server.NettyRpcServer;
import top.grayson.serviceimpl.HelloServiceImpl2;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/20 15:35
 * @Description
 */
@RpcScan(basePackage = "top.grayson")
public class NettyServerMain {
    public static void main(String[] args) {
        //  1. 注册服务
        //  1.1 根据注解注册服务
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyServerMain.class);
        NettyRpcServer nettyRpcServer = (NettyRpcServer) applicationContext.getBean("nettyRpcServer");

        //  1.2 手动注册服务
        HelloService helloService2 = new HelloServiceImpl2();
        RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                .group("test2")
                .version("version2")
                .service(helloService2).build();
        nettyRpcServer.registerService(rpcServiceConfig);
        nettyRpcServer.start();
    }
}
