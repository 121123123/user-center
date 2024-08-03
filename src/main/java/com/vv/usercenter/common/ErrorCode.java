package com.vv.usercenter.common;

/**
 * 错误码
 *
 * @author vv先森
 * @create 2024-07-09 13:47
 */
public enum ErrorCode {
    SUCCESS(0,"ok",""),

    PARAMS_ERROR(40000,"请求参数错误",""),

    NULL_ERROR(40001,"请求数据为空",""),

    NO_LOGIN(40100,"未登录",""),

    NO_REGISTER(40102,"注册失败",""),

    NO_AUTH(40101,"无权限",""),

    Fail_UPDATE(40110,"修改失败",""),

    Fail_DELETE(40111,"删除失败","");

    private final int code;

    private final String message;

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
