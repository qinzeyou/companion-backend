package com.caiya.companion.exception;

import com.caiya.companion.common.ErrorCode;

/**
 * @author caiya
 * @description 业务异常类
 * @create 2024-05-27 13:31
 */
public class BusinessException extends RuntimeException{

    private final int code;
    private final String description;

    public BusinessException(int code, String message, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }

    public BusinessException(ErrorCode errorCode, String description) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
