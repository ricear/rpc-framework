package top.grayson.util;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/19 17:18
 * @Description 属性文件工具类
 */
@Slf4j
public class PropertyFileUtils {
    public PropertyFileUtils() {
    }

    /**
     * 读取属性文件，获取属性信息
     * @param fileName  属性文件名称
     * @return  属性信息
     */
    public static Properties readPropertiesFile(String fileName) {
        URL url = Thread.currentThread()
                .getContextClassLoader()
                .getResource("");
        String rpcConfigPath = "";
        if (url != null) {
            rpcConfigPath = url.getPath() + fileName;
        }
        Properties properties = null;
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(rpcConfigPath), StandardCharsets.UTF_8)) {
            properties = new Properties();
            properties.load(reader);
        } catch (IOException e) {
            log.error("Occur exception when reading property files [{}]", rpcConfigPath);
        }
        return properties;
    }
}
