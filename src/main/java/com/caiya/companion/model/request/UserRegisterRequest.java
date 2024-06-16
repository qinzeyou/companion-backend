package com.caiya.companion.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author caiya
 * @description 用户注册请求体
 * @create 2024-05-05 09:03
 */
@Data
public class UserRegisterRequest implements Serializable {
    private String userAccount;
    private String password;
    private String checkPassword;
}
