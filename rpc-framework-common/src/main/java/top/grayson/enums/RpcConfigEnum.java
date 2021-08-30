package top.grayson.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/19 17:27
 * @Description RPC 配置枚举类
 */
@AllArgsConstructor
@Getter
public enum RpcConfigEnum {
    RPC_CONFIG_PATH("rpc.properties"),
    ZK_ADDRESS("rpc.zookeeper.address");

    private final String propertyValue;
}
