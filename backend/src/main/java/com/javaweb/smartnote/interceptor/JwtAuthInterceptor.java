package com.javaweb.smartnote.interceptor;

import com.javaweb.smartnote.common.JwtUtil;
import com.javaweb.smartnote.common.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("请求缺少Authorization头: {} {}", request.getMethod(), request.getRequestURI());
            response.setStatus(401);
            return false;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            log.warn("Token无效或已过期: {} {}", request.getMethod(), request.getRequestURI());
            response.setStatus(401);
            return false;
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        UserContext.setUserId(userId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
