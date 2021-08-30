package top.grayson.remoting.transport.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.grayson.config.CustomShutdownHook;
import top.grayson.config.RpcServiceConfig;
import top.grayson.factory.SingletonFactory;
import top.grayson.provider.ServiceProvider;
import top.grayson.provider.impl.ZkServiceProviderImpl;
import top.grayson.remoting.constant.RpcConstants;
import top.grayson.remoting.transport.netty.codec.RpcMessageDecoder;
import top.grayson.remoting.transport.netty.codec.RpcMessageEncoder;
import top.grayson.util.RuntimeUtils;
import top.grayson.util.concurrent.threadpool.ThreadPoolFactoryUtils;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/19 15:13
 * @Description
 */
@Slf4j
@Component
public class NettyRpcServer {
    private final ServiceProvider serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);

    /**
     * 注册服务
     * @param rpcServiceConfig  RPC 服务器配置信息
     */
    public void registerService(RpcServiceConfig rpcServiceConfig) {
        serviceProvider.publishService(rpcServiceConfig);
    }

    @SneakyThrows
    public void start() {
        CustomShutdownHook.getCustomShutdownHook()
                .clearAll();
        String host = InetAddress.getLocalHost().getHostAddress();
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        DefaultEventExecutorGroup serviceHandlerGroup = new DefaultEventExecutorGroup(
                RuntimeUtils.cpus() * 2,
                ThreadPoolFactoryUtils.createThreadFactory("service-handler-group", false)
        );
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    //  TCP 默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据块，减少网络传输，TCP_NODELAY 参数就是控制是否启用 Nagle 算法
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    //  是否开启 TCP 底层心跳机制
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    //  表示系统用于临时存放已完成三次握手的请求的队列的最大长度，如果连接建立频繁，服务器创建新连接较慢，可以适当调大这个参数
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    //  当客户端第一次请求的时候才会进行初始化
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            //  30 秒内没有收到客户端请求的话就关闭连接
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS))
                                    .addLast(new RpcMessageEncoder())
                                    .addLast(new RpcMessageDecoder())
                                    .addLast(serviceHandlerGroup, new NettyRpcServerHandler());
                        }
                    });

            //  绑定端口，同步等待绑定成功
            ChannelFuture channelFuture = serverBootstrap.bind(host, RpcConstants.SERVER_PORT).sync();
            //  等待服务端监听端口关闭
            channelFuture.channel()
                    .closeFuture()
                    .sync();
        } catch (InterruptedException e) {
            log.error("Occur exception when start server: {}", e);
        } finally {
            log.error("Shut down boos group and worker group.");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            serviceHandlerGroup.shutdownGracefully();
        }

    }
}
