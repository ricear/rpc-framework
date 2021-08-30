package top.grayson.serialize.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.sun.xml.internal.ws.encoding.soap.SerializationException;
import top.grayson.exception.SerializeException;
import top.grayson.remoting.dto.RpcRequest;
import top.grayson.remoting.dto.RpcResponse;
import top.grayson.serialize.Serializer;

import javax.sql.rowset.serial.SerialException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/20 10:15
 * @Description Kryo 序列化器
 */
public class KryoSerializer implements Serializer {
    /**
     * 因为 Kryo 不是线程安全的，所以使用 ThreadLocal 来存储 Kryo 对象
     */
    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.register(RpcRequest.class);
        kryo.register(RpcResponse.class);
        return kryo;
    });

    /**
     * 将对象序列化为字节数组
     * @param obj   要序列化的对象
     * @return  对象序列化后的字节数组
     */
    @Override
    public byte[] serialize(Object obj) {
        try (
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                Output output = new Output(byteArrayOutputStream)
        ) {
            Kryo kryo = kryoThreadLocal.get();
            //  将对象序列化为字节数组
            kryo.writeObject(output, obj);
            kryoThreadLocal.remove();
            return output.toBytes();
        } catch (Exception e) {
            throw new SerializeException("Serialization failed.");
        }
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
        try (
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                Input input = new Input(byteArrayInputStream)
        ) {
            Kryo kryo = kryoThreadLocal.get();
            //  从字节数组反序列化出对象
            Object object = kryo.readObject(input, clazz);
            kryoThreadLocal.remove();
            return clazz.cast(object);
        } catch (IOException e) {
            throw new SerializeException("Deserialization failed.");
        }
    }
}
