package com.javaweb.smartnote.common;

import lombok.Data;

// 统一响应封装：code + msg + data
@Data
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;

    private Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(200, msg, data);
    }

    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }

    public static <T> Result<T> error(ResultCodeEnum codeEnum) {
        return new Result<>(codeEnum.getCode(), codeEnum.getMsg(), null);
    }

    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null);
    }
}
