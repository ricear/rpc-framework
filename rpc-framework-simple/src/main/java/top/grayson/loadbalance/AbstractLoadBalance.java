package top.grayson.loadbalance;

import top.grayson.remoting.dto.RpcRequest;

import java.util.List;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/19 16:26
 * @Description 抽象负载均衡类
 */
public abstract class AbstractLoadBalance implements LoadBalance{
    /**
     * 从服务地址列表中选出一个服务地址
     * @param serviceAddresses  服务地址列表
     * @param rpcRequest    RPC 请求
     * @return  目标服务地址
     */
    @Override
    public String selectServiceAddress(List<String> serviceAddresses, RpcRequest rpcRequest) {
        if (null == serviceAddresses || serviceAddresses.size() == 0) {
            return null;
        }
        if (serviceAddresses.size() == 1) {
            return serviceAddresses.get(0);
        }
        return doSelect(serviceAddresses, rpcRequest);
    }

    /**
     * 执行选择
     * @param serviceAddresses  服务地址列表
     * @param rpcRequest    RPC 请求
     * @return  目标服务地址
     */
    protected abstract String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest);
}
