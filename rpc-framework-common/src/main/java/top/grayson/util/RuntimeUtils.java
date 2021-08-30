package top.grayson.util;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/20 11:27
 * @Description
 */
public class RuntimeUtils {
    /**
     * 获取 CPU 的核心数
     * @return  CPU 核心数
     */
    public static int cpus() {
        return Runtime.getRuntime().availableProcessors();
    }
}
