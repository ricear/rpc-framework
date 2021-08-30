package top.grayson.compress;

import top.grayson.extension.SPI;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/19 11:08
 * @Description 压缩接口，所有压缩类都要实现这个接口
 */
@SPI
public interface Compress {
    /**
     * 压缩
     * @param bytes 要压缩的字节数组
     * @return  压缩后的字节数组
     */
    byte[] compress(byte[] bytes);

    /**
     * 解压缩后的字节数组
     * @param bytes 要解压缩的字节数组
     * @return  解压缩后的字节数组
     */
    byte[] decompress(byte[] bytes);
}
