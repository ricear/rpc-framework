package top.grayson;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.grayson.annotation.RpcReference;

import java.sql.Time;
import java.util.Date;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/20 15:56
 * @Description
 */
@Component
@Slf4j
public class HelloController {
    @RpcReference(version = "version1", group = "test1")
    private HelloService helloService;

    static {
        log.info("HelloController was created successfully.");
    }

    public void test() {
        for (int i = 0; i < 10; i++) {
            log.info(helloService.hello(new Hello("111", "222")));
        }
    }
}
