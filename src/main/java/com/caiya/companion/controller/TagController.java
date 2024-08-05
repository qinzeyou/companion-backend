package com.caiya.companion.controller;

import com.caiya.companion.common.BaseResponse;
import com.caiya.companion.common.ErrorCode;
import com.caiya.companion.common.ResultUtils;
import com.caiya.companion.exception.BusinessException;
import com.caiya.companion.model.domain.User;
import com.caiya.companion.service.TagService;
import com.caiya.companion.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author caiya
 * @description 标签控制层
 * @create 2024-08-05 21:21
 */
@RequestMapping("/tag")
@RestController
public class TagController {

    @Resource
    private TagService tagService;
    @Resource
    private UserService userService;

    /**
     * 新增标签
     * @param tagName 标签名称
     * @return 新增标签的id
     */
    @PostMapping()
    private BaseResponse<Long> addTag(@RequestParam("tagName") String tagName, HttpServletRequest request) {
        if (StringUtils.isEmpty(tagName)) throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签名称不能为空");
        User loginUser = userService.getLoginUser(request);
        Long res = tagService.addTag(tagName, loginUser);
        return ResultUtils.success(res);
    }
}
