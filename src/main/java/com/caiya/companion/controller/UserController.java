package com.caiya.companion.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caiya.companion.common.BaseResponse;
import com.caiya.companion.common.ErrorCode;
import com.caiya.companion.common.ResultUtils;
import com.caiya.companion.exception.BusinessException;
import com.caiya.companion.model.domain.User;
import com.caiya.companion.model.request.UserLoginRequest;
import com.caiya.companion.model.request.UserRegisterRequest;
import com.caiya.companion.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * @author caiya
 * @description 用户控制层
 * @create 2024-05-05 09:02
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 注册接口
     *
     * @param userRegisterRequest
     * @return
     */
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

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String password = userLoginRequest.getPassword();
        // 判断参数是否为空
        if (StringUtils.isAnyBlank(userAccount, password)) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User user = userService.userLogin(userAccount, password, request);
        return ResultUtils.success(user);
    }

    /**
     * 获取当前用户信息
     *
     * @param request
     * @return
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        // 旧版
//        User currentUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
//        long userId = currentUser.getId();
//        User dbUser = userService.getById(userId);
//        User safetyUser = userService.getSafetyUser(dbUser);
//        return ResultUtils.success(safetyUser);

        // 新版
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(loginUser);
    }

    /**
     * 条件搜索用户
     *
     * @param username
     * @param request
     * @return
     */
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        if (!userService.isAdmin(request)) throw new BusinessException(ErrorCode.NO_AUTH);
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
     * 推荐用户列表
     *
     * @param pageNum
     * @param pageSize
     * @return
     */
    // todo 未实现推荐
    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageNum, long pageSize, HttpServletRequest request) {
        Page<User> userPage = userService.recommendUsers(pageNum, pageSize, request);
        return ResultUtils.success(userPage);
    }

    /**
     * 推荐匹配用户列表
     *
     * @param num
     * @param request
     * @return
     */
    @GetMapping("/match/{num}")
    public BaseResponse<List<User>> matchUsers(@PathVariable Integer num, HttpServletRequest request) {
        if (num <= 0) {
            num = 10;
        }
        List<User> userPage = userService.matchUsers(num, request);
        return ResultUtils.success(userPage);
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

    /**
     * 根据id删除用户，只有管路员才能删除
     *
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) throw new BusinessException(ErrorCode.NO_AUTH);
        if (id < 0) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        Boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 退出登录
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        int res = userService.userLogout(request);
        return ResultUtils.success(res);
    }

    @PutMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        if (user == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        int result = userService.updateUser(user, request);
        return ResultUtils.success(result);
    }
}
