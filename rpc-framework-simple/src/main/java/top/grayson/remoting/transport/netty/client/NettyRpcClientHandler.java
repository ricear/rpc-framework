package top.grayson.remoting.transport.netty.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import top.grayson.enums.CompressTypeEnum;
import top.grayson.enums.SerializationTypeEnum;
import top.grayson.factory.SingletonFactory;
import top.grayson.remoting.constant.RpcConstants;
import top.grayson.remoting.dto.RpcMessage;
import top.grayson.remoting.dto.RpcResponse;
import top.grayson.serialize.Serializer;

import java.net.InetSocketAddress;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/19 14:09
 * @Description 自定义 ChannelHandler 处理服务器发来的数据
 */
@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {
    private final UnprocessedRequests unprocessedRequests;
    private final NettyRpcClient nettyRpcClient;

    public NettyRpcClientHandler() {
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.nettyRpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
    }

    /**
     * 读取从服务器传输过来的数据
     * @param ctx   ChannelHandler 上下文
     * @param msg   服务器传输过来的数据
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            log.info("Client receive msg: [{}]", msg);
            if (msg instanceof RpcMessage) {
                RpcMessage rpcMessage = (RpcMessage) msg;
                byte messageType = rpcMessage.getMessageType();
                if (messageType == RpcConstants.HEART_BEAT_RESPONSE_TYPE) {
                    log.info("Heart [{}]", rpcMessage.getData());
                } else if (messageType == RpcConstants.RESPONSE_TYPE) {
                    RpcResponse<Object> rpcResponse = (RpcResponse<Object>) rpcMessage.getData();
                    unprocessedRequests.complete(rpcResponse);
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 用户事件触发
     * @param ctx   ChannelHandler 上下文
     * @param evt   用户事件
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                log.info("Writer idle happen [{}]", ctx.channel().remoteAddress());
                Channel channel = nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                RpcMessage rpcMessage = RpcMessage.builder()
                        .codec(SerializationTypeEnum.PROTOSTUFF.getCode())
                        .compress(CompressTypeEnum.GZIP.getCode())
                        .messageType(RpcConstants.HEART_BEAT_REQUEST_TYPE)
                        .data(RpcConstants.PING).build();
                channel.writeAndFlush(rpcMessage)
                        .addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * 当处理客户端消息发生异常时调用
     * @param ctx   ChannelHandler 上下文
     * @param cause 异常原因
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Client catch exception: {}", cause);
        ctx.close();
    }
}
