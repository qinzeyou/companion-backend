package com.caiya.companion.controller;

import com.caiya.companion.common.BaseResponse;
import com.caiya.companion.common.ResultUtils;
import com.caiya.companion.service.UploadService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * @author caiya
 * @description 描述
 * @create 2024-08-05 12:02
 */
@RequestMapping("/upload")
@RestController
public class UploadController {
    @Resource
    private UploadService uploadService;

    @PostMapping("/file")
    public BaseResponse<String> uploadFile(@RequestBody MultipartFile file) {
        String url = uploadService.uploadFile(file);
        return ResultUtils.success(url);
    }
}
