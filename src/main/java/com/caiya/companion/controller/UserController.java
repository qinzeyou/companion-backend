package com.caiya.companion.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caiya.companion.common.*;
import com.caiya.companion.exception.BusinessException;
import com.caiya.companion.model.domain.User;
import com.caiya.companion.model.request.UserLoginRequest;
import com.caiya.companion.model.request.UserRegisterRequest;
import com.caiya.companion.model.vo.UserVO;
import com.caiya.companion.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.caiya.companion.constant.UserConstant.USER_LOGIN_STATE;


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
        // 判断参数是否为空
        if (StringUtils.isAnyBlank(userAccount, password)) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        Long result = userService.userRegister(userAccount, password);
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
    public BaseResponse<UserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String password = userLoginRequest.getPassword();
        // 判断参数是否为空
        if (StringUtils.isAnyBlank(userAccount, password)) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        UserVO user = userService.userLogin(userAccount, password, request);
        return ResultUtils.success(user);
    }

    /**
     * 获取当前登录用户信息
     *
     * @param request
     * @return
     */
    @GetMapping("/current")
    public BaseResponse<UserVO> getCurrentUser(HttpServletRequest request) {
        UserVO loginUser = userService.getCurrentUser(request);
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
    @GetMapping("/recommend")
    public BaseResponse<PageResponse<List<UserVO>>> recommendUsers(long pageNum, long pageSize, HttpServletRequest request) {
        PageResponse<List<UserVO>> userPage = userService.recommendUsers(pageNum, pageSize, request);
        return ResultUtils.success(userPage);
    }

    /**
     * 推荐匹配用户列表
     *
     * @param num 匹配用户的数量
     * @param request 登录信息
     * @return 匹配的用户列表
     */
    @GetMapping("/match/{num}")
    public BaseResponse<List<UserVO>> matchUsers(@PathVariable Integer num, HttpServletRequest request) {
        if (num <= 0) {
            num = 10;
        }
        // 获取用户信息
        UserVO loginUser = (UserVO) request.getSession().getAttribute(USER_LOGIN_STATE);
        // 用户信息转换：在redis预热代码中也需要匹配用户，但是传入的参数是user，而不是脱敏的用户信息，所以为了减少重复代码，统一转换为user
        User user = new User();
        BeanUtils.copyProperties(loginUser, user);
        List<UserVO> userPage = userService.matchUsers(num, user);
        return ResultUtils.success(userPage);
    }

    /**
     * 根据标签搜索用户
     *
     * @param tagIdList 标签id列表
     * @return
     */
    @GetMapping("/search/tagIds")
    public BaseResponse<List<UserVO>> searchUserByTagIds(@RequestParam(required = false) List<Integer> tagIdList) {
        if (CollectionUtils.isEmpty(tagIdList)) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        List<UserVO> userList = userService.searchUserByTagIds(tagIdList);
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

    /**
     * 根据用户id获取用户信息
     * @param userId 用户id
     * @return 用户信息
     */
    @GetMapping("/getUserInfo")
    public BaseResponse<UserVO> getUserInfoById(@RequestParam Long userId) {
        if (userId == null) throw new BusinessException(ErrorCode.NULL_ERROR, "用户id不能为空");
        UserVO res = userService.getUserInfoById(userId);
        return ResultUtils.success(res);
    }
}
