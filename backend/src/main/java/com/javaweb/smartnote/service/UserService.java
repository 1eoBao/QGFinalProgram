package com.javaweb.smartnote.service;

import com.javaweb.smartnote.dto.request.*;
import com.javaweb.smartnote.dto.response.UserInfoResponse;
import com.javaweb.smartnote.entity.User;

public interface UserService {

    void register(RegisterRequest request);

    String login(LoginRequest request);

    UserInfoResponse getUserInfo(Long userId);

    void updateUserInfo(Long userId, UpdateUserRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    User getById(Long userId);
}
