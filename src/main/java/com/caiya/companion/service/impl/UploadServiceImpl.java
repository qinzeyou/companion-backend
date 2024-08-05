package com.caiya.companion.service.impl;

import com.caiya.companion.common.ErrorCode;
import com.caiya.companion.exception.BusinessException;
import com.caiya.companion.service.UploadService;
import com.caiya.companion.utils.ImageUtil;
import com.caiya.companion.utils.QiniuUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author caiya
 * @description 上传文件相关操作接口的实现类
 * @create 2024-08-05 11:58
 */
@Service
public class UploadServiceImpl implements UploadService {
    @Resource
    private QiniuUtils qiniuUtils;
    @Resource
    private ImageUtil imageUtil;

    @Override
    public String uploadFile(MultipartFile file) {
        if (file.isEmpty()) throw new BusinessException(ErrorCode.NULL_ERROR);
        String fileName = imageUtil.resetFileName(file);
        try {
            FileInputStream uploadFile = (FileInputStream) file.getInputStream();
            String path = qiniuUtils.upload(uploadFile, fileName);
            return path;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
