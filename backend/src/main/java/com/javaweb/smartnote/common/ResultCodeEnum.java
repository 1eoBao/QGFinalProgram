package com.javaweb.smartnote.common;

import lombok.Getter;

@Getter
public enum ResultCodeEnum {
    SUCCESS(200, "成功"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或Token失效"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "数据冲突"),
    INTERNAL_ERROR(500, "服务器内部错误");

    private final Integer code;
    private final String msg;

    ResultCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}