package top.grayson.exception;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/20 10:24
 * @Description 序列化异常类
 */
public class SerializeException extends RuntimeException {
    public SerializeException(final String message) {
        super(message);
    }
}
