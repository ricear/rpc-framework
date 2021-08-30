package top.grayson.loadbalance.loadbalancer;

import top.grayson.loadbalance.AbstractLoadBalance;
import top.grayson.remoting.dto.RpcRequest;

import java.util.List;
import java.util.Random;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/19 17:01
 * @Description 随机负载均衡器
 */
public class RandomLoadBalance extends AbstractLoadBalance {
    /**
     * 执行选择
     * @param serviceAddresses  服务地址列表
     * @param rpcRequest    RPC 请求
     * @return  目标服务地址
     */
    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest) {
        Random random = new Random();
        return serviceAddresses.get(random.nextInt(serviceAddresses.size()));
    }
}
