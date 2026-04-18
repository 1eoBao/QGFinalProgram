package com.javaweb.smartnote.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.javaweb.smartnote.common.BusinessException;
import com.javaweb.smartnote.common.JwtUtil;
import com.javaweb.smartnote.common.ResultCodeEnum;
import com.javaweb.smartnote.dto.request.*;
import com.javaweb.smartnote.dto.response.UserInfoResponse;
import com.javaweb.smartnote.entity.User;
import com.javaweb.smartnote.mapper.UserMapper;
import com.javaweb.smartnote.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 用户注册：校验密码一致性、唯一性，BCrypt加密后入库
    @Override
    public void register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR, "两次密码不一致");
        }

        // 检查邮箱唯一性
        Long emailCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getEmail, request.getEmail()));
        if (emailCount > 0) {
            throw new BusinessException(ResultCodeEnum.CONFLICT, "邮箱已被注册");
        }

        // 检查手机号唯一性
        Long phoneCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone()));
        if (phoneCount > 0) {
            throw new BusinessException(ResultCodeEnum.CONFLICT, "手机号已被注册");
        }

        // 检查用户名唯一性
        Long usernameCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (usernameCount > 0) {
            throw new BusinessException(ResultCodeEnum.CONFLICT, "用户名已被占用");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getUsername());

        userMapper.insert(user);
        log.info("用户注册成功: username={}", request.getUsername());
    }

    // 用户登录：支持邮箱/手机号登录，校验密码后生成JWT
    @Override
    public String login(LoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getEmail, request.getAccount())
                        .or()
                        .eq(User::getPhone, request.getAccount()));

        if (user == null) {
            throw new BusinessException(ResultCodeEnum.UNAUTHORIZED, "账号或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCodeEnum.UNAUTHORIZED, "账号或密码错误");
        }

        log.info("用户登录成功: userId={}", user.getId());
        return jwtUtil.generateToken(user.getId());
    }

    // 获取用户个人信息
    @Override
    public UserInfoResponse getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "用户不存在");
        }
        return toUserInfoResponse(user);
    }

    // 修改个人信息：昵称、头像、座右铭
    @Override
    public void updateUserInfo(Long userId, UpdateUserRequest request) {
        User user = new User();
        user.setId(userId);
        user.setNickname(request.getNickname());
        user.setAvatar(request.getAvatar());
        user.setMotto(request.getMotto());
        userMapper.updateById(user);
        log.info("用户信息更新: userId={}", userId);
    }

    // 修改密码：校验旧密码后更新
    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "用户不存在");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR, "旧密码错误");
        }

        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(updateUser);
        log.info("用户修改密码: userId={}", userId);
    }

    @Override
    public User getById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    // Entity → Response DTO 转换
    private UserInfoResponse toUserInfoResponse(User user) {
        UserInfoResponse resp = new UserInfoResponse();
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setEmail(user.getEmail());
        resp.setPhone(user.getPhone());
        resp.setNickname(user.getNickname());
        resp.setAvatar(user.getAvatar());
        resp.setMotto(user.getMotto());
        resp.setCreateTime(user.getCreateTime());
        resp.setUpdateTime(user.getUpdateTime());
        return resp;
    }
}
