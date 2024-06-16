package com.caiya.companion.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用返回类
 *
 * @author caiya
 */
@Data
public class BaseResponse<T> implements Serializable {
    /**
     * 状态码
     */
    private int code;
    /**
     * 返回数据
     */
    private T data;
    /**
     * 信息
     */
    private String msg;
    /**
     * 详细描述
     */
    private String description;

    public BaseResponse() {

    }

    public BaseResponse(int code, T data, String msg, String description) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.description = description;
    }

    public BaseResponse(int code, T data, String message) {
        this(code, data, message, "");
    }

    public BaseResponse(int code, T data) {
        this(code, data, "", "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getDescription(), errorCode.getDescription());
    }
}
