package top.grayson.serviceimpl;

import lombok.extern.slf4j.Slf4j;
import top.grayson.Hello;
import top.grayson.HelloService;
import top.grayson.annotation.RpcService;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/20 15:46
 * @Description
 */
@Slf4j
@RpcService(group = "test1", version = "version1")
public class HelloServiceImpl implements HelloService {
    static {
        log.info("HelloServiceImpl was created successfully.");
    }

    @Override
    public String hello(Hello hello) {
        log.info("HelloServiceImpl get message: {}", hello.getMessage());
        String result = "Hello description is " + hello.getDescription();
        log.info("HelloServiceImpl return: {}", result);
        return result;
    }
}
