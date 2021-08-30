package top.grayson.annotation;

import java.lang.annotation.*;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/20 14:53
 * @Description RPC 引用注解，自动装配服务的实现类
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface RpcReference {
    /**
     * 服务版本，默认为空
     * @return  服务版本
     */
    String version() default "";

    /**
     * 服务所属组，默认为空
     * @return  服务所属组
     */
    String group() default "";
}
