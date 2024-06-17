package com.caiya.companion.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.caiya.companion.common.BaseResponse;
import com.caiya.companion.common.ErrorCode;
import com.caiya.companion.common.ResultUtils;
import com.caiya.companion.exception.BusinessException;
import com.caiya.companion.model.domain.User;
import com.caiya.companion.model.request.UserLoginRequest;
import com.caiya.companion.model.request.UserRegisterRequest;
import com.caiya.companion.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.caiya.companion.constant.UserConstant.ADMIN_ROLE;
import static com.caiya.companion.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author caiya
 * @description 用户控制层
 * @create 2024-05-05 09:02
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String password = userRegisterRequest.getPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        // 判断参数是否为空
        if (StringUtils.isAnyBlank(userAccount, password, checkPassword)) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        Long result = userService.userRegister(userAccount, password, checkPassword);
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) return null;
        String userAccount = userLoginRequest.getUserAccount();
        String password = userLoginRequest.getPassword();
        // 判断参数是否为空
        if (StringUtils.isAnyBlank(userAccount, password)) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User user = userService.userLogin(userAccount, password, request);
        return ResultUtils.success(user);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        long userId = currentUser.getId();
        User dbUser = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(dbUser);
        return ResultUtils.success(safetyUser);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        if (!isAdmin(request)) throw new BusinessException(ErrorCode.NO_AUTH);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 根据用户昵称模糊查询
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        // 返回脱敏的用户数组
        List<User> userList = userService.list(queryWrapper).stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(userList);
    }

    /**
     * 根据标签搜索用户
     *
     * @param tagNameList 标签列表
     * @return
     */
    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUserByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        List<User> userList = userService.searchUserByTags(tagNameList);
        return ResultUtils.success(userList);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        if (!isAdmin(request)) throw new BusinessException(ErrorCode.NO_AUTH);
        if (id < 0) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        Boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        int res = userService.userLogout(request);
        return ResultUtils.success(res);
    }

    /**
     * 判断是否为管理员
     *
     * @param request
     * @return
     */
    private boolean isAdmin(HttpServletRequest request) {
        // 从session中获取用户信息
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }
}
