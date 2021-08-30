package top.grayson.remoting.handler;

import lombok.extern.slf4j.Slf4j;
import top.grayson.exception.RpcException;
import top.grayson.factory.SingletonFactory;
import top.grayson.provider.ServiceProvider;
import top.grayson.provider.impl.ZkServiceProviderImpl;
import top.grayson.remoting.dto.RpcRequest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/20 12:06
 * @Description RPC 请求处理器
 */
@Slf4j
public class RpcRequestHandler {
    private ServiceProvider serviceProvider;

    public RpcRequestHandler() {
        this.serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
    }

    /**
     * 通过动态代理执行客户端需要执行的目标方法，然后返回相应结果
     * @param rpcRequest    RPC 请求
     * @return  客户端需要执行的目标方法的执行结果
     */
    public Object handle(RpcRequest rpcRequest) {
        Object service = serviceProvider.getService(rpcRequest.getRpcServiceName());
        return invokeTargetMethod(rpcRequest, service);
    }

    /**
     * 通过动态代理执行客户端需要执行的目标方法，然后返回相应结果
     * @param rpcRequest    RPC 请求
     * @param service   客户端需要执行的目标方法
     * @return  客户端需要执行的目标方法的执行结果
     */
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) {
        Object result;
        try {
            Method method = service.getClass()
                    .getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            result = method.invoke(service, rpcRequest.getParameters());
            log.info("Service: [{}] successful, invoke method: [{}]", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RpcException(e.getMessage(), e);
        }
        return result;
    }
}
