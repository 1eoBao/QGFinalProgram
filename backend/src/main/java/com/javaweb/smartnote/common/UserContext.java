package com.javaweb.smartnote.common;

// 用户上下文：基于ThreadLocal存储当前请求的userId，由JwtAuthInterceptor设置和清理
public class UserContext {
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    public static void setUserId(Long id) {
        USER_ID.set(id);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    // 请求结束后必须清理，防止内存泄漏
    public static void clear() {
        USER_ID.remove();
    }
}
