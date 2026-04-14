package com.javaweb.smartnote.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

// 全局异常处理器：捕获各类异常，返回统一格式的友好错误信息
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 业务异常：返回具体错误码和消息
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, msg={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    // 参数校验异常（@Valid 触发）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", msg);
        return Result.error(ResultCodeEnum.PARAM_ERROR.getCode(), msg);
    }

    // 绑定异常
    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e) {
        String msg = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return Result.error(ResultCodeEnum.PARAM_ERROR.getCode(), msg);
    }

    // 运行时异常：不暴露堆栈信息，返回友好提示
    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: ", e);
        String msg = e.getMessage() != null ? e.getMessage() : "系统繁忙，请稍后再试";
        return Result.error(ResultCodeEnum.INTERNAL_ERROR.getCode(), msg);
    }

    // 其他异常
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.error(ResultCodeEnum.INTERNAL_ERROR);
    }
}
