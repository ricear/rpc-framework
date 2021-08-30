package top.grayson.serialize.protostuff;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import top.grayson.serialize.Serializer;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/20 10:32
 * @Description
 */
public class ProtostuffSerializer implements Serializer {
    /**
     * 避免每次序列化时重新分配缓冲空间
     */
    private static final LinkedBuffer BUFFER = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);

    /**
     * 将对象序列化为字节数组
     * @param obj   要序列化的对象
     * @return  对象序列化后的字节数组
     */
    @Override
    public byte[] serialize(Object obj) {
        Class<?> clazz = obj.getClass();
        Schema schema = RuntimeSchema.getSchema(clazz);
        byte[] bytes;
        try {
            bytes = ProtostuffIOUtil.toByteArray(obj, schema, BUFFER);
        } finally {
            BUFFER.clear();
        }
        return bytes;
    }

    /**
     * 从字节数组反序列化出对象
     * @param bytes 序列化后的字节数组
     * @param clazz 目标类
     * @param <T>   目标类类型
     * @return  字节数组反序列化出的对象
     */
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        Schema<T> schema = RuntimeSchema.getSchema(clazz);
        T object = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(bytes, object, schema);
        return object;
    }
}
