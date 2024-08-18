package com.caiya.companion.model.request;

import lombok.Data;

/**
 * @Author caiya
 * @Description 修改公告请求体
 * @Version 1.0
 */
@Data
public class NoticeUpdateRequest {
    /**
     * 主键
     */
    private Long id;

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
}
