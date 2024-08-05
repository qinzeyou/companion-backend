package com.caiya.companion.utils;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;

/**
 * @author caiya
 * @description 七牛云上传文件工具类
 * @create 2024-08-05 11:51
 */
@Component
public class QiniuUtils {
    @Autowired
    private UploadManager uploadManager;
    @Autowired
    private Auth auth;

    @Value("${oss.qiniu.bucketName}")
    private String bucketName;
    @Value("${oss.qiniu.path}")
    private String url;

    /**
     * 七牛云上传文件
     *
     * @param file     文件
     * @param fileName 文件名称
     * @return 文件url
     * @throws QiniuException
     */
    public String upload(FileInputStream file, String fileName) throws QiniuException {
        String token = auth.uploadToken(bucketName);
        // images/：存储空间下的文件夹名称，需要先在七牛云空间中创建
        fileName = "images/" + fileName;
        Response res = uploadManager.put(file, fileName, token, null, null);
        if (!res.isOK()) {
            throw new RuntimeException("上传七牛云出错:" + res);
        }
        return "http://" + url + "/" + fileName;
    }
}
