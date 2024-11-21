package com.y.datingsite.common;

/**
 * 错误码
 * @author : Yuan
 * @date :2024/5/6
 */
public enum ErrorCode {
    SUCCESS(0,"ok"," "),
    PARAMS_ERROR(40000,"请求参数错误",""),
    NULL_ERROR(40001,"请求的数据为空",""),
    NOT_LOGIN(40100,"未登录",""),
    NOT_AUTN(40101,"无权限",""),
    // 表示请求被拒绝，通常用于操作被禁止的情况。这可能是由于安全规则阻止了某些操作，或者用户尝试执行不允许的行为。
    FORBIDDEN(40301,"禁止操作",""),
    SYSTEM_ERROR(50000,"系统内部异常","");

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
