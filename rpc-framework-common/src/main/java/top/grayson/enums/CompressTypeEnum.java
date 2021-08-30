package top.grayson.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/18 17:55
 * @Description 压缩类型枚举类
 */
@AllArgsConstructor
@Getter
public enum CompressTypeEnum {
    GZIP((byte) 0x01, "gzip");

    private final byte code;
    private final String name;

    /**
     * 根据压缩编码获取压缩类型
     * @param code  压缩编码
     * @return  压缩编码对应的压缩类型
     */
    public static String getName(byte code) {
        for (CompressTypeEnum compressTypeEnum: CompressTypeEnum.values()) {
            if (compressTypeEnum.getCode() == code) {
                return compressTypeEnum.getName();
            }
        }
        return null;
    }
}
