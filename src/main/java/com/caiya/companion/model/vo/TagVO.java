package com.caiya.companion.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author caiya
 * @description 脱敏后的标签信息
 * @create 2024-08-06 12:12
 */
@Data
public class TagVO {
    /**
     * id
     */
    private Long id;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 权重，控制用户自身标签的显示顺序，权重越高，标签越靠前
     */
    private Integer weight;

    /**
     * 创建者id
     */
    private Long userId;

    /**
     * 父标签id
     */
    private Long parentId;

    /**
     * 是否父标签，0-不是，1-父标签
     */
    private Boolean isParent;

    /**
     * 使用人数
     */
    private Integer userNumber;

    /**
     * 标签文字颜色
     */
    private String textColor;

    /**
     * 背景颜色
     */
    private String color;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
