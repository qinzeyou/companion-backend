package com.caiya.companion.model.vo;

import lombok.Data;

/**
 * @author caiya
 * @description 用户标签脱敏
 * @create 2024-08-06 16:41
 */
@Data
public class UserTagVO {
    /**
     * 控制标签的显示顺序，数值范围（0 - 10）
     */
    private Integer weight;

    // 非数据库字段
    private TagVO tag;
}
