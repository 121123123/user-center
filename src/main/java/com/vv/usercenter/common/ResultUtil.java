package com.vv.usercenter.common;

/**
 * @author vv先森
 * @create 2024-07-09 14:15
 */
public class ResultUtil {
    public static BaseResponse error(ErrorCode errorCode) {
        return new BaseResponse(errorCode);
    }

    public static BaseResponse error(int errorCode, String message, String description) {
        return new BaseResponse(errorCode, null, message, description);
    }

    public static BaseResponse error(ErrorCode errorCode, String message, String description) {
        return new BaseResponse(errorCode.getCode(), null, message, description);
    }

    public static BaseResponse error(ErrorCode errorCode, String description) {
        return new BaseResponse(errorCode.getCode(), null, errorCode.getMessage(), description);
    }
}
