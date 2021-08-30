package top.grayson.compress.gzip;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import top.grayson.compress.Compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author peng.wei
 * @version 1.0
 * @date 2021/8/20 16:32
 * @Description
 */
public class GzipCompress implements Compress {
    private static final int BUFFER_SIZE = 1024 * 4;

    /**
     * 对数据进行压缩
     * @param bytes 要压缩的字节数组
     * @return  压缩后的数据
     */
    @Override
    public byte[] compress(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("Bytes are null.");
        }
        try (
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                GZIPOutputStream gzip = new GZIPOutputStream(byteArrayOutputStream);
        ) {
            gzip.write(bytes);
            gzip.flush();
            gzip.finish();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Gzip compress error", e);
        }
    }

    /**
     * 对数据进行解压缩
     * @param bytes 要解压缩的字节数组
     * @return  解压缩后的数据
     */
    @Override
    public byte[] decompress(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("Bytes are null.");
        }
        try (
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(bytes))
        ) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int n;
            while ((n = gzip.read(buffer)) > -1) {
                byteArrayOutputStream.write(buffer, 0, n);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Gzip decompress error", e);
        }
    }
}
