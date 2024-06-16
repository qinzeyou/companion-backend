package com.caiya.companion.common;

/**
 * @author caiya
 * @description 返回结果码
 * @create 2024-05-27 13:03
 */
public enum ErrorCode {

    SUCCESS(200, "success", ""),
    PARAMS_ERROR(40000, "请求参数错误", ""),
    NULL_ERROR(40001, "请求数据为空", ""),
    NOT_LOGIN(40100, "未登录", ""),
    NO_AUTH(40101, "无权限", ""),
    SYSTEM_ERROR(50000, "系统内部异常", "")
    ;

    /**
     * 状态码
     */
    private final int code;
    /**
     * 返回信息
     */
    private final String message;
    /**
     * 详细描述
     */
    private final String description;

    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }
}
