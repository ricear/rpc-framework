package top.grayson.remoting.transport.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import top.grayson.enums.CompressTypeEnum;
import top.grayson.enums.SerializationTypeEnum;
import top.grayson.extension.ExtensionLoader;
import top.grayson.factory.SingletonFactory;
import top.grayson.registry.ServiceDiscovery;
import top.grayson.remoting.constant.RpcConstants;
import top.grayson.remoting.dto.RpcMessage;
import top.grayson.remoting.dto.RpcRequest;
import top.grayson.remoting.dto.RpcResponse;
import top.grayson.remoting.transport.RpcRequestTransport;
import top.grayson.remoting.transport.netty.codec.RpcMessageDecoder;
import top.grayson.remoting.transport.netty.codec.RpcMessageEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/18 16:09
 * @Description Netty 客户端
 */
@Slf4j
public class NettyRpcClient implements RpcRequestTransport {
    private final ServiceDiscovery serviceDiscovery;
    private final UnprocessedRequests unprocessedRequests;
    private final ChannelProvider channelProvider;
    private final Bootstrap bootstrap;
    private EventLoopGroup eventLoopGroup;

    public NettyRpcClient() {
        //  初始化相关资源。例如 EventLoopGroup、Bootstrap
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                //  连接超时时间，如果超过了这个时间连接还没有建立，那么连接建立失败
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        //  如果 15 秒钟内还没有数据发送，就会发送一个心跳到服务器
                        pipeline.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS))
                                .addLast(new RpcMessageEncoder())
                                .addLast(new RpcMessageDecoder())
                                .addLast(new NettyRpcClientHandler());
                    }
                });
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zk");
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
    }

    /**
     * 连接服务器，获取 Channel，以便我们可以发送数据到服务器
     *
     * @param inetSocketAddress 服务器地址
     * @return Channel
     */
    @SneakyThrows
    public Channel doConnect(InetSocketAddress inetSocketAddress) {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap
                .connect(inetSocketAddress)
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        log.info("The client has connected [{}] successful!", inetSocketAddress.toString());
                        completableFuture.complete(future.channel());
                    } else {
                        throw new IllegalStateException(String.format("The client has connected [%s] failed!", inetSocketAddress.toString()));
                    }
                });
        return completableFuture.get();
    }

    /**
     * 发送 RPC 请求
     * @param rpcRequest    RPC 请求
     * @return  响应消息
     */
    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest);

        Channel channel = getChannel(inetSocketAddress);
        if (channel.isActive()) {
            //  放到未处理请求中
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
            //  构建消息
            RpcMessage rpcMessage = RpcMessage.builder()
                    .data(rpcRequest)
                    .codec(SerializationTypeEnum.PROTOSTUFF.getCode())
                    .compress(CompressTypeEnum.GZIP.getCode())
                    .messageType(RpcConstants.REQUEST_TYPE).build();
            //  发送消息
            channel
                    .writeAndFlush(rpcMessage)
                    .addListener((ChannelFutureListener) future -> {
                        if (future.isSuccess()) {
                            log.info("Client send message: [{}]", rpcMessage);
                        } else {
                            future.channel().close();
                            resultFuture.completeExceptionally(future.cause());
                            log.error("Client send failed: ", future.cause());
                        }
                    });
        } else {
            throw new IllegalStateException();
        }
        return resultFuture;
    }

    /**
     * 获取 Channel
     * @param inetSocketAddress 服务器地址
     * @return  Channel
     */
    public Channel getChannel(InetSocketAddress inetSocketAddress) {
        Channel channel = channelProvider.get(inetSocketAddress);
        if (channel == null) {
            channel = doConnect(inetSocketAddress);
            channelProvider.set(inetSocketAddress, channel);
        }
        return channel;
    }

    /**
     * 关闭用户事件
     */
    public void close() {
        eventLoopGroup.shutdownGracefully();
    }
}
