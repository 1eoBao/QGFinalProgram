package com.javaweb.smartnote.controller;

import com.javaweb.smartnote.common.Result;
import com.javaweb.smartnote.common.UserContext;
import com.javaweb.smartnote.dto.request.*;
import com.javaweb.smartnote.dto.response.UserInfoResponse;
import com.javaweb.smartnote.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户模块")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 用户注册
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success(null);
    }

    // 用户登录（支持邮箱/手机号），返回JWT Token
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody LoginRequest request) {
        String token = userService.login(request);
        return Result.success(token);
    }

    // 获取当前登录用户的个人信息
    @Operation(summary = "获取个人信息")
    @GetMapping("/info")
    public Result<UserInfoResponse> getUserInfo() {
        Long userId = UserContext.getUserId();
        return Result.success(userService.getUserInfo(userId));
    }

    // 修改个人信息（昵称、头像、座右铭）
    @Operation(summary = "修改个人信息")
    @PutMapping("/info")
    public Result<Void> updateUserInfo(@RequestBody UpdateUserRequest request) {
        Long userId = UserContext.getUserId();
        userService.updateUserInfo(userId, request);
        return Result.success(null);
    }

    // 修改密码（需验证旧密码）
    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = UserContext.getUserId();
        userService.changePassword(userId, request);
        return Result.success(null);
    }
}
