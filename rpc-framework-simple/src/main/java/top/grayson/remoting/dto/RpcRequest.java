package top.grayson.remoting.dto;

import lombok.*;

import java.io.Serializable;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/18 15:18
 * @Description 客户端请求实体类
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = 1710449332383447426L;
    private String requestId;
    private String interfaceName;
    private String methodName;
    private Object[] parameters;
    private Class<?>[] paramTypes;
    private String version;
    private String group;

    public String getRpcServiceName() {
        return this.getInterfaceName() + this.getGroup() + this.getVersion();
    }
}
