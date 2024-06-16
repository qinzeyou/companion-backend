package com.caiya.companion.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author caiya
 * @description 用户登录请求体
 * @create 2024-05-05 09:03
 */
@Data
public class UserLoginRequest implements Serializable {
    private String userAccount;
    private String password;
}
