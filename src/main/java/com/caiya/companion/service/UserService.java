package com.caiya.companion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.caiya.companion.model.domain.User;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author Administrator
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2024-05-04 20:38:32
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userAccount 账号
     * @param password 密码
     * @param checkPassword 校验密码
     * @return 新用户id
     */
    long userRegister(String userAccount, String password, String checkPassword);

    /**
     * 登录
     * @param userAccount 账号
     * @param password 密码
     * @param request http请求
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String password, HttpServletRequest request);

    /**
     * 用户数据脱敏
     *
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);


    /**
     * 根据标签列表查询用户
     *
     * @param tagNameList 标签列表
     * @return
     */
    List<User> searchUserByTags(List<String> tagNameList);
}
