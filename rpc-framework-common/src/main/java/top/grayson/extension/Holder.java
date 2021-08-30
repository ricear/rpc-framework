package top.grayson.extension;

import lombok.Getter;
import lombok.Setter;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/19 10:13
 * @Description
 */
public class Holder<T> {
    private volatile T value;

    public T get() {
        return this.value;
    }

    public void set(final T value) {
        this.value = value;
    }
}
