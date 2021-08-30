package top.grayson.factory;

import lombok.NoArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/19 14:13
 * @Description 获取单例对象的工厂类
 */
@NoArgsConstructor
public class SingletonFactory {
    private static final Map<String, Object> OBJECT_MAP = new ConcurrentHashMap<>();

    /**
     * 获取单例类
     * @param clazz 要获取单例的类
     * @param <T>   要获取单例的类的类型
     * @return  指定类型的单例
     */
    public static <T> T getInstance(Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Class cannot be null.");
        }

        String key = clazz.toString();
        if (OBJECT_MAP.containsKey(key)) {
            return clazz.cast(OBJECT_MAP.get(key));
        } else {
            try {
                T instance = clazz.getDeclaredConstructor().newInstance();
                OBJECT_MAP.put(key, instance);
                return clazz.cast(instance);
            } catch (Exception e) {
                throw new RuntimeException("Create instance error: {}", e);
            }
        }
    }
}
