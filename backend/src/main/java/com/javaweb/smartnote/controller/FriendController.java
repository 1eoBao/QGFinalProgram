package com.javaweb.smartnote.controller;

import com.javaweb.smartnote.common.Result;
import com.javaweb.smartnote.common.UserContext;
import com.javaweb.smartnote.dto.request.FriendApplyHandleRequest;
import com.javaweb.smartnote.dto.request.FriendGroupCreateRequest;
import com.javaweb.smartnote.dto.response.FriendApplyVO;
import com.javaweb.smartnote.dto.response.FriendGroupVO;
import com.javaweb.smartnote.dto.response.FriendVO;
import com.javaweb.smartnote.dto.response.UserInfoResponse;
import com.javaweb.smartnote.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "好友模块")
@RestController
@RequestMapping("/api/friend")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    // 搜索用户（按邮箱/手机号精确匹配）
    @Operation(summary = "搜索用户（邮箱/手机号）")
    @GetMapping("/search")
    public Result<List<UserInfoResponse>> searchUser(@RequestParam String keyword) {
        Long userId = UserContext.getUserId();
        return Result.success(friendService.searchUser(keyword, userId));
    }

    // 发送好友申请
    @Operation(summary = "发送好友申请")
    @PostMapping("/apply")
    public Result<Void> sendApply(@RequestParam Long toUserId) {
        Long userId = UserContext.getUserId();
        friendService.sendApply(userId, toUserId);
        return Result.success(null);
    }

    // 处理好友申请（同意/拒绝）
    @Operation(summary = "处理好友申请")
    @PutMapping("/apply/{id}")
    public Result<Void> handleApply(@PathVariable Long id, @Valid @RequestBody FriendApplyHandleRequest request) {
        Long userId = UserContext.getUserId();
        friendService.handleApply(userId, id, request);
        return Result.success(null);
    }

    // 获取收到的好友申请列表
    @Operation(summary = "获取好友申请列表")
    @GetMapping("/apply/list")
    public Result<List<FriendApplyVO>> getApplyList() {
        Long userId = UserContext.getUserId();
        return Result.success(friendService.getApplyList(userId));
    }

    // 获取好友列表（可按分组筛选）
    @Operation(summary = "获取好友列表")
    @GetMapping("/list")
    public Result<List<FriendVO>> getFriendList(@RequestParam(required = false) Long groupId) {
        Long userId = UserContext.getUserId();
        return Result.success(friendService.getFriendList(userId, groupId));
    }

    // 获取好友分组列表
    @Operation(summary = "获取好友分组列表")
    @GetMapping("/group/list")
    public Result<List<FriendGroupVO>> getGroupList() {
        Long userId = UserContext.getUserId();
        return Result.success(friendService.getGroupList(userId));
    }

    // 创建好友分组
    @Operation(summary = "创建好友分组")
    @PostMapping("/group")
    public Result<Void> createGroup(@Valid @RequestBody FriendGroupCreateRequest request) {
        Long userId = UserContext.getUserId();
        friendService.createGroup(userId, request);
        return Result.success(null);
    }

    // 删除好友分组
    @Operation(summary = "删除好友分组")
    @DeleteMapping("/group/{id}")
    public Result<Void> deleteGroup(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        friendService.deleteGroup(userId, id);
        return Result.success(null);
    }

    // 移动好友到指定分组
    @Operation(summary = "移动好友到分组")
    @PutMapping("/{friendId}/group")
    public Result<Void> moveFriendToGroup(@PathVariable Long friendId, @RequestParam Long groupId) {
        Long userId = UserContext.getUserId();
        friendService.moveFriendToGroup(userId, friendId, groupId);
        return Result.success(null);
    }
}
