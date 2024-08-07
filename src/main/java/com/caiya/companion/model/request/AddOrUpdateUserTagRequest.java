package com.caiya.companion.model.request;

import lombok.Data;

/**
 * @author caiya
 * @description 用户【添加||修改】标签请求体
 * @create 2024-08-06 15:59
 */
@Data
public class AddOrUpdateUserTagRequest {
    private Long tagId;
    private Integer weight;
    private String color;
    private String textColor;
}
