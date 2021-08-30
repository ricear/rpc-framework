package top.grayson.remoting.transport.netty.client;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/18 16:56
 * @Description 存储和获取 Channel 对象
 */
@Slf4j
public class ChannelProvider {
    private final Map<String, Channel> channelMap;

    public ChannelProvider() {
        this.channelMap = new ConcurrentHashMap<>();
    }

    /**
     * 根据 地址 获取 Channel 对象
     * @param inetSocketAddress 地址
     * @return  Channel 对象
     */
    public Channel get(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        //  判断当前地址是否已经建立了连接
        if (channelMap.containsKey(key)) {
            Channel channel = channelMap.get(key);
            if (null != channel && channel.isActive()) {
                return channel;
            } else {
                channelMap.remove(key);
            }
        }
        return null;
    }

    /**
     * 将 Channel 缓存到 ChannelMap 中
     * @param inetSocketAddress 服务器地址
     * @param channel   Channel
     */
    public void set(InetSocketAddress inetSocketAddress, Channel channel) {
        String key = inetSocketAddress.toString();
        channelMap.put(key, channel);
        log.info("Channel add: [{}], current map size is : [{}]", key, channelMap.size());
    }

    /**
     * 移除 Channel
     * @param inetSocketAddress 服务器地址
     */
    public void remove(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        channelMap.remove(key);
        log.info("Channel remove: [{}], current map size is : [{}]", key, channelMap.size());
    }
}
