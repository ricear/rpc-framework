package top.grayson.config;

import lombok.*;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/19 15:18
 * @Description
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcServiceConfig {
    /**
     * 服务版本
     */
    private String version = "";
    /**
     * 当这个接口有多个实现类的时候，通过 group 来进行区分
     */
    private String group = "";
    /**
     * 目标服务
     */
    private Object service;

    /**
     * 获取 RPC 服务名称
     * @return  RPC 服务名称
     */
    public String getRpcServiceName() {
        return this.getServiceName() + this.getGroup() + this.getVersion();
    }

    /**
     * 获取目标服务名称
     * @return  目标服务名称
     */
    public String getServiceName() {
        return this.getService().getClass().getInterfaces()[0].getCanonicalName();
    }
}
