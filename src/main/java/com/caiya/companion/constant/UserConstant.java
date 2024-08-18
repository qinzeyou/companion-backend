package com.caiya.companion.constant;

import com.caiya.companion.utils.UserUtils;

/**
 * @author caiya
 * @description 用户常量
 * @create 2024-05-05 10:04
 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    public static final String USER_LOGIN_STATE = "userLoginState";

    // 权限
    // 普通用户
    int DEFAULT_ROLE = 0;
    // 管理员
    int ADMIN_ROLE = 1;

    /**
     * 随机昵称
     */
    String RANDOM_USERNAME = UserUtils.getRandomUsername(5);
    /**
     * 默认头像
     */
    String DEFAULT_USER_AVATAR = "http://shq2sf8gm.hn-bkt.clouddn.com/images/5b331c6f52954af1a720d3728dd1e831.jpg";
    /**
     * 默认用户简介
     */
    String DEFAULT_USER_PROFILE = "用户很忙，还没有自我介绍";
}
