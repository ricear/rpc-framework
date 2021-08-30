package top.grayson.loadbalance;

import top.grayson.extension.SPI;
import top.grayson.remoting.dto.RpcRequest;

import java.util.List;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/19 16:24
 * @Description 负载均衡接口
 */
@SPI
public interface LoadBalance {
    /**
     * 从服务地址列表中选出一个服务地址
     * @param serviceAddresses  服务地址列表
     * @param rpcRequest    RPC 请求
     * @return  目标服务地址
     */
    String selectServiceAddress(List<String> serviceAddresses, RpcRequest rpcRequest);
}
