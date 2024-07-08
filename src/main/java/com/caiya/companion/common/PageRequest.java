package com.caiya.companion.common;

import lombok.Data;

/**
 * @author caiya
 * @description 通用请求分页
 * @create 2024-07-08 21:53
 */
@Data
public class PageRequest {
    /**
     * 当前页
     */
    protected int pageNum = 1;
    /**
     * 一页多少条数据
     */
    protected int pageSize = 10;
}
