package com.caiya.companion.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author caiya
 * @description 上传相关操作接口类
 * @create 2024-08-05 11:56
 */
public interface UploadService {
    String uploadFile(MultipartFile file);
}
