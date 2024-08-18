package com.caiya.companion.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author caiya
 * @description 脱敏后的公告信息
 * @create 2024-08-06 12:12
 */
@Data
public class NoticeVO implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 发布者id
     */
    private Long userId;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 权重，权重越大越靠前（1-10）
     */
    private Integer weight;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    // 非数据库字段
    // 创建者信息
    private UserVO createUser;
}
