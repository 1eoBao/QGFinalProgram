package com.javaweb.smartnote.common;

public interface Constants {
    // Token
    long TOKEN_EXPIRE_MS = 14L * 24 * 60 * 60 * 1000;

    // 笔记权限
    int PERM_PRIVATE = 0;
    int PERM_FRIEND_READ = 1;
    int PERM_FRIEND_EDIT = 2;
    int PERM_PUBLIC = 3;

    // 好友申请状态
    int APPLY_PENDING = 0;
    int APPLY_ACCEPT = 1;
    int APPLY_REJECT = 2;

    // 笔记权限详情类型
    int PERM_TYPE_READ = 1;
    int PERM_TYPE_EDIT = 2;
}