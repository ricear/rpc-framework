package top.grayson.remoting.transport.netty.server;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import top.grayson.enums.CompressTypeEnum;
import top.grayson.enums.RpcResponseCodeEnum;
import top.grayson.enums.SerializationTypeEnum;
import top.grayson.factory.SingletonFactory;
import top.grayson.remoting.constant.RpcConstants;
import top.grayson.remoting.dto.RpcMessage;
import top.grayson.remoting.dto.RpcRequest;
import top.grayson.remoting.dto.RpcResponse;
import top.grayson.remoting.handler.RpcRequestHandler;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/20 11:44
 * @Description 自定义服务器的 ChannelHandler 来处理客户端发来的数据
 */
@Slf4j
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {
    private RpcRequestHandler rpcRequestHandler;

    public NettyRpcServerHandler() {
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

    /**
     * 读取从客户端传输过来的数据
     * @param ctx   ChannelHandler 上下文
     * @param msg   从客户端发来的数据
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof RpcMessage) {
                log.info("Server receive msg: {}", msg);
                byte messageType = ((RpcMessage) msg).getMessageType();
                RpcMessage rpcMessage = RpcMessage.builder()
                        .codec(SerializationTypeEnum.PROTOSTUFF.getCode())
                        .compress(CompressTypeEnum.GZIP.getCode()).build();
                if (messageType == RpcConstants.HEART_BEAT_REQUEST_TYPE) {
                    rpcMessage.setMessageType(RpcConstants.HEART_BEAT_RESPONSE_TYPE);
                    rpcMessage.setData(RpcConstants.PONG);
                } else {
                    RpcRequest rpcRequest = (RpcRequest) ((RpcMessage) msg).getData();
                    //  执行客户端需要执行的目标方法，然后返回相应的结果
                    Object result = rpcRequestHandler.handle(rpcRequest);
                    log.info(String.format("Server get result: %s", result.toString()));
                    rpcMessage.setMessageType(RpcConstants.RESPONSE_TYPE);
                    if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                        RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
                        rpcMessage.setData(rpcResponse);
                    } else {
                        RpcResponse<Object> rpcResponse = RpcResponse.fail(RpcResponseCodeEnum.FAIL);
                        rpcMessage.setData(rpcResponse);
                        log.error("Channel not writable now, message dropped.");
                    }
                }
                ctx.writeAndFlush(rpcMessage)
                        .addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } finally {
            //  确保 ByteBuf 已经释放，否则可能会导致内存泄漏
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
            if (state == IdleState.READER_IDLE) {
                log.info("Idle check happen, so close the connection.");
                ctx.close();
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
        log.error("Server catch exception.", cause);
        ctx.close();
    }
}
