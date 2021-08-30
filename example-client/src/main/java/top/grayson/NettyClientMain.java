package top.grayson;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import top.grayson.annotation.RpcScan;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/20 15:54
 * @Description
 */
@RpcScan(basePackage = {"top.grayson"})
public class NettyClientMain {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyClientMain.class);
        HelloController helloController = (HelloController) applicationContext.getBean("helloController");
        helloController.test();
    }
}
