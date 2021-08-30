package top.grayson.remoting.transport.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import top.grayson.compress.Compress;
import top.grayson.enums.CompressTypeEnum;
import top.grayson.enums.SerializationTypeEnum;
import top.grayson.extension.ExtensionLoader;
import top.grayson.remoting.constant.RpcConstants;
import top.grayson.remoting.dto.RpcMessage;
import top.grayson.remoting.dto.RpcRequest;
import top.grayson.remoting.dto.RpcResponse;
import top.grayson.serialize.Serializer;

import java.util.Arrays;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/19 11:39
 * @Description RPC 消息解码器
 */
@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {
    public RpcMessageDecoder() {
        /**
         * lengthFieldOffset: magic code 为 4 字节，version 为 1 字节，然后是总长度，因此该值为 5
         * lengthFieldLength: 总长度为 4 字节，因此该值为 4
         * lengthAdjustment: 总长度长包括所有数据并读取之前的 9 个字节，所以左边的长度是 (fullLength - 9)。所以该值为 -9
         * initialBytesToStrip: 我们会手动检查 magic code 和 version，因此不需要跳过任何字节，所以该值为 0
         */
      this(RpcConstants.MAX_FRAME_LENGTH, 5, 4, -9, 0);
    }

    /**
     * RPC 消息解码器构造函数
     * @param maxFrameLength    最大帧长度，他决定了客户端能接收消息的最大长度，超过这个限制的数据将会被丢弃
     * @param lengthFieldOffset 长度字段偏移量，长度字段保存了字节的指定长度
     * @param lengthFieldLength 长度字段的长度
     * @param lengthAdjustment  要添加到长度字段的补偿值
     * @param initialBytesToStrip   跳过的字节数：
     *                              如果我们要接收所有的 header + body 数据，这个值为0
     *                              如果我们仅仅想接收 body 数据，我们需要跳过 header 消耗的字节数
     */
    public RpcMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    /**
     * 对帧进行解码
     * @param ctx   ChannelHander 上下文
     * @param in    字节缓冲数据
     * @return  解码后的数据
     * @throws Exception
     */
    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded = super.decode(ctx, in);
        if (decoded instanceof ByteBuf) {
            ByteBuf frame = (ByteBuf) decoded;
            if (frame.readableBytes() >= RpcConstants.TOTAL_LENGTH) {
                try {
                    return decodeFrame(frame);
                } catch (Exception e) {
                    log.error("Decode frame error!", e);
                } finally {
                    frame.release();
                }
            }
        }
        return decoded;
    }

    /**
     * 对 RPC 消息进行解码
     * @param frame RPC 消息帧
     * @return  解码后的 RPC 消息
     */
    private Object decodeFrame(ByteBuf frame) {
        //  一定要按照顺序读取 ByteBuf
        checkMagicNumber(frame);
        checkVersion(frame);
        int fullLength = frame.readInt();

        //  构建 RpcMessage 对象
        byte messageType = frame.readByte();
        byte codecType = frame.readByte();
        byte compressType = frame.readByte();
        int requestId = frame.readInt();
        RpcMessage rpcMessage = RpcMessage.builder()
                .codec(codecType)
                .requestId(requestId)
                .messageType(messageType).build();
        if (messageType == RpcConstants.HEART_BEAT_REQUEST_TYPE) {
            rpcMessage.setData(RpcConstants.PING);
            return rpcMessage;
        }
        if (messageType == RpcConstants.HEART_BEAT_RESPONSE_TYPE) {
            rpcMessage.setData(RpcConstants.PONG);
            return rpcMessage;
        }

        int bodyLength = fullLength - RpcConstants.HEAD_LENGTH;
        if (bodyLength > 0) {
            byte[] bs = new byte[bodyLength];
            frame.readBytes(bs);
            //  对 body 字节进行解压
            String compressName = CompressTypeEnum.getName(compressType);
            Compress compress = ExtensionLoader.getExtensionLoader(Compress.class)
                    .getExtension(compressName);
            bs = compress.decompress(bs);
            //  对对象进行反序列化
            String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
                    .getExtension(codecName);
            if (messageType == RpcConstants.REQUEST_TYPE) {
                RpcRequest tmpvalue = serializer.deserialize(bs, RpcRequest.class);
                rpcMessage.setData(tmpvalue);
            } else {
                RpcResponse tmpvalue = serializer.deserialize(bs, RpcResponse.class);
                rpcMessage.setData(tmpvalue);
            }
        }
        return rpcMessage;
    }

    /**
     * 检查 version
     * @param frame RPC 消息帧
     */
    private void checkVersion(ByteBuf frame) {
        //  读取 version 并进行比较
        byte version = frame.readByte();
        if (version != RpcConstants.VERSION) {
            throw new IllegalArgumentException("Version is not compatible: " + version);
        }
    }

    /**
     * 检查 magic number
     * @param frame RPC 消息帧
     */
    private void checkMagicNumber(ByteBuf frame) {
        //  读取 magic number （前 4 个字节），然后进行比较
        int len = RpcConstants.MAGIC_NUMBER.length;
        byte[] tmp = new byte[len];
        frame.readBytes(tmp);
        for (int i = 0; i < len; i++) {
            if (tmp[i] != RpcConstants.MAGIC_NUMBER[i]) {
                throw new IllegalArgumentException("Unknown magic number code: " + Arrays.toString(tmp));
            }
        }
    }
}
