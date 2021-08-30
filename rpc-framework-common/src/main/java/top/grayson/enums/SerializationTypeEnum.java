package top.grayson.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/19 09:53
 * @Description
 */
@AllArgsConstructor
@Getter
public enum SerializationTypeEnum {
    KYRO((byte) 0x01, "kyro"),
    PROTOSTUFF((byte) 0x02, "protostuff");

    private final byte code;
    private final String name;

    public static String getName(byte code) {
        for (SerializationTypeEnum serializationTypeEnum: SerializationTypeEnum.values()) {
            if (serializationTypeEnum.getCode() == code) {
                return serializationTypeEnum.getName();
            }
        }
        return null;
    }
}
