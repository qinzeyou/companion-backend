package com.caiya.companion.utils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caiya.companion.common.PageResponse;
import org.springframework.stereotype.Component;

/**
 * @author caiya
 * @description 数据转换操作工具类
 * @create 2024-08-08 13:09
 */
@Component
public class DataTransitionUtils {

    /**
     * 将mybatis-plus分页转为自定义分页
     * @param page 分页数据
     * @param data 自定义的分页数据
     * @return 自定义分页
     */
    public <T, K> PageResponse<T> pageResponse(Page<K> page, T data) {
        PageResponse<T> response = new PageResponse<>();
        response.setTotal(page.getTotal());
        response.setSize(page.getSize());
        response.setCurrent(page.getCurrent());
        response.setRecords(data);
        return response;
    }
}
