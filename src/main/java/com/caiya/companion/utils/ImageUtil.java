package com.caiya.companion.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

/**
 * @author caiya
 * @description 图片处理工具类
 * @create 2024-08-05 11:53
 */
@Component
public class ImageUtil {
    // 生成图片名称
    public String resetFileName(MultipartFile file){
        String originalFileName = file.getOriginalFilename();
        String ext = "." + originalFileName.split("\\.")[1];
        String uuid = UUID.randomUUID().toString().replace("-","");
        return uuid + ext;
    }

    /**
     * UUID随机生成图片名称
     * @param fileName
     * @return
     */
    public  String getRandomImgName(String fileName) {
        int index = fileName.lastIndexOf(".");
        if (fileName.isEmpty() || index == -1){
            throw new IllegalArgumentException();
        }
        // 获取文件后缀
        String suffix = fileName.substring(index).toLowerCase();
        // 生成UUID
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        // 生成上传至云服务器的路径
        return "image:" + uuid + suffix;
    }

    /**
     * 图片转为byte数组
     * @param path
     * @return
     * @throws IOException
     */

    public  byte[] image2byte(String path) throws IOException {
        byte[] data = null;
        URL url = null;
        InputStream input = null;
        try{
            url = new URL(path);
            HttpURLConnection httpUrl = (HttpURLConnection) url.openConnection();
            httpUrl.connect();
            httpUrl.getInputStream();
            input = httpUrl.getInputStream();
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int numBytesRead = 0;
        while ((numBytesRead = input.read(buf)) != -1) {
            output.write(buf, 0, numBytesRead);
        }
        data = output.toByteArray();
        output.close();
        input.close();
        return data;
    }
}
