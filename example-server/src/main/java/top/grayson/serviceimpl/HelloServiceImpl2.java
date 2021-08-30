package top.grayson.serviceimpl;

import lombok.extern.slf4j.Slf4j;
import top.grayson.Hello;
import top.grayson.HelloService;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/20 15:46
 * @Description
 */
@Slf4j
public class HelloServiceImpl2 implements HelloService {
    static {
        log.info("HelloServiceImpl2 was created successfully.");
    }

    @Override
    public String hello(Hello hello) {
        log.info("HelloServiceImpl2 get message: {}", hello.getMessage());
        String result = "Hello description is " + hello.getDescription();
        log.info("HelloServiceImpl2 return: {}", result);
        return result;
    }
}
