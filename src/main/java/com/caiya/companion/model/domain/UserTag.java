package com.caiya.companion.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户标签关系表
 * @TableName user_tag
 */
@TableName(value ="user_tag")
@Data
public class UserTag implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 使用者id
     */
    private Long userId;

    /**
     * 标签id
     */
    private Long tagId;

    /**
     * 控制标签的显示顺序，数值范围（0 - 10）
     */
    private Integer weight;

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}