package com.caiya.companion.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author caiya
 * @description 描述
 * @create 2024-08-04 21:54
 */
@Data
public class PageResponse<T> implements Serializable {
    private T records;
    private long current;
    private long size;
    private long total;
}
