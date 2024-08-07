package com.caiya.companion.controller;

import com.caiya.companion.common.BaseResponse;
import com.caiya.companion.common.ErrorCode;
import com.caiya.companion.common.ResultUtils;
import com.caiya.companion.exception.BusinessException;
import com.caiya.companion.model.domain.User;
import com.caiya.companion.model.request.AddOrUpdateUserTagRequest;
import com.caiya.companion.service.UserService;
import com.caiya.companion.service.UserTagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author caiya
 * @description 用户标签关系控制层
 * @create 2024-08-07 11:36
 */
@RequestMapping("/userTag")
@RestController
@Slf4j
public class UserTagController {

    @Resource
    private UserTagService userTagService;
    @Resource
    private UserService userService;

    /**
     * 用户添加自身标签
     * @param addUserTagRequest 请求体
     * @param request 登录信息
     * @return 操作结果：true成功，false失败
     */
    @PostMapping()
    private BaseResponse<Boolean> addUserTag(@RequestBody AddOrUpdateUserTagRequest addUserTagRequest, HttpServletRequest request) {
        if (addUserTagRequest == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Boolean res = userTagService.addUserTag(addUserTagRequest, loginUser);
        return ResultUtils.success(res);
    }

    /**
     * 用户修改自身标签
     * @param addUserTagRequest 请求体
     * @param request 登录信息
     * @return 操作结果：true成功，false失败
     */
    @PutMapping()
    private BaseResponse<Boolean> updateUserTag(@RequestBody AddOrUpdateUserTagRequest addUserTagRequest, HttpServletRequest request) {
        if (addUserTagRequest == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Boolean res = userTagService.updateUserTag(addUserTagRequest, loginUser);
        return ResultUtils.success(res);
    }

    /**
     * 用户移除自己的标签（单个移除）
     * @param tagId 标签id
     * @param request 登录信息
     * @return 操作结果：true成功，false失败
     */
    @DeleteMapping("/{tagId}")
    private BaseResponse<Boolean> removeUserTag(@PathVariable Long tagId, HttpServletRequest request) {
        if (tagId <= 0) throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签id错误");
        User loginUser = userService.getLoginUser(request);
        Boolean res = userTagService.removeUserTag(tagId, loginUser);
        return ResultUtils.success(res);
    }

}
