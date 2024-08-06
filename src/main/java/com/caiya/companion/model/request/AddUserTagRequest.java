package com.caiya.companion.model.request;

import lombok.Data;

/**
 * @author caiya
 * @description 用户添加自身标签请求体
 * @create 2024-08-06 15:59
 */
@Data
public class AddUserTagRequest {
    private Long tagId;
    private Integer weight;
}
