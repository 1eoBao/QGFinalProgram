package com.javaweb.smartnote.common;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final Integer code;

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResultCodeEnum codeEnum) {
        super(codeEnum.getMsg());
        this.code = codeEnum.getCode();
    }

    public BusinessException(ResultCodeEnum codeEnum, String message) {
        super(message);
        this.code = codeEnum.getCode();
    }
}
