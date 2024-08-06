package com.caiya.companion.model.request;

import lombok.Data;

/**
 * @author caiya
 * @description 修改标签请求体
 * @create 2024-08-05 21:24
 */
@Data
public class TagUpdateRequest {
    private Long id;
    private String tagName;
    private Long parentId;
    private Boolean isParent;
    private Integer status;
    private String textColor;
    private String color;
}
