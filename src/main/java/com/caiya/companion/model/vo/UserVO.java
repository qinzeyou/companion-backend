package com.caiya.companion.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.caiya.companion.model.domain.UserTag;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author caiya
 * @description 用户包装类（脱敏）
 * @create 2024-07-09 08:13
 */
@Data
public class UserVO implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
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
     * 标签 json 列表
     */
    private String tags;

    /**
     * 用户角色 0 普通用户 1 管理员
     */
    private Integer userRole;

    /**
     * 更新时间
     */
    private Date updateTime;

    // 非数据库字段
    /**
     * 用户标签列表
     */
    private List<TagVO> userTags;
}
