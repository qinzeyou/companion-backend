package com.caiya.companion.common;

/**
 * @author caiya
 * @description 返回结果工具类
 * @create 2024-05-27 12:35
 */
public class ResultUtils {
    /**
     * 返回成功请求
     * @param data 请求结果
     * @return
     * @param <T>
     */
    public static <T> BaseResponse<T>  success(T data) {
        return new BaseResponse<>(200,  data, "success", "");
    }

    /**
     * 返回错误请求
     * @param errorCode
     * @return
     */
    public static BaseResponse error(ErrorCode errorCode) {
        return new BaseResponse(errorCode);
    }


    /**
     * 返回错误请求
     * @param errorCode
     * @return
     */
    public static BaseResponse error(ErrorCode errorCode, String description) {
        return new BaseResponse(errorCode.getCode(), null, errorCode.getMessage(), description);
    }

    /**
     * 返回错误请求
     * @param errorCode
     * @return
     */
    public static BaseResponse error(ErrorCode errorCode, String message, String description) {
        return new BaseResponse(errorCode.getCode(), null, message, description);
    }

    /**
     * 返回错误请求
     *
     * @return
     */
    public static BaseResponse error(int code, String message, String description) {
        return new BaseResponse(code, null, message, description);
    }
}
