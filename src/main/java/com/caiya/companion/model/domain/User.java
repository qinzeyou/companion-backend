package com.caiya.companion.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String password;

    /**
     * 电话
     */
    private String phone;

    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 描述
     */
    private String profile;


    /**
     * 状态 0-正常
     */
    private Integer userStatus;

    /**
     * 创建时间
     */
    private Date createTime;


    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 标签 json 列表
     */
    private String tags;

    /**
     * 用户角色 0 普通用户 1 管理员
     */
    private Integer userRole;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}