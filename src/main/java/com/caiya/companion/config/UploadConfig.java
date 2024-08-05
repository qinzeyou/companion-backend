package com.caiya.companion.config;

import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author caiya
 * @description OSS上传文件配置类
 * @create 2024-08-05 11:45
 */
@Configuration
public class UploadConfig {
    @Value("${oss.qiniu.accessKey}")
    private String accessKey;
    @Value("${oss.qiniu.secretKey}")
    private String secretKey;

    @Bean
    public Auth getAuth(){
        return Auth.create(accessKey,secretKey);
    }

    @Bean
    public UploadManager getUploadManager(){
        return new UploadManager(new com.qiniu.storage.Configuration());
    }
}
