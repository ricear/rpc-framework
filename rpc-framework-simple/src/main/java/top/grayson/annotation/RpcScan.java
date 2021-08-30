package top.grayson.annotation;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/20 14:22
 * @Description 扫描自定义的注解
 */

import org.springframework.context.annotation.Import;
import top.grayson.spring.CustomScanner;
import top.grayson.spring.CustomScannerRegistrar;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Import(CustomScannerRegistrar.class)
@Documented
public @interface RpcScan {
    String[] basePackage();
}
