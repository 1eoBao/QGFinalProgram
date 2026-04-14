package com.javaweb.smartnote.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.javaweb.smartnote.common.BusinessException;
import com.javaweb.smartnote.common.Constants;
import com.javaweb.smartnote.common.ResultCodeEnum;
import com.javaweb.smartnote.dto.request.FriendApplyHandleRequest;
import com.javaweb.smartnote.dto.request.FriendGroupCreateRequest;
import com.javaweb.smartnote.dto.response.FriendApplyVO;
import com.javaweb.smartnote.dto.response.FriendVO;
import com.javaweb.smartnote.dto.response.UserInfoResponse;
import com.javaweb.smartnote.entity.Friend;
import com.javaweb.smartnote.entity.FriendApply;
import com.javaweb.smartnote.entity.FriendGroup;
import com.javaweb.smartnote.entity.User;
import com.javaweb.smartnote.mapper.FriendApplyMapper;
import com.javaweb.smartnote.mapper.FriendGroupMapper;
import com.javaweb.smartnote.mapper.FriendMapper;
import com.javaweb.smartnote.mapper.UserMapper;
import com.javaweb.smartnote.service.FriendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private final FriendMapper friendMapper;
    private final FriendApplyMapper friendApplyMapper;
    private final FriendGroupMapper friendGroupMapper;
    private final UserMapper userMapper;

    // 搜索用户：按邮箱或手机号精确匹配，排除自己
    @Override
    public List<UserInfoResponse> searchUser(String keyword, Long currentUserId) {
        List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .ne(User::getId, currentUserId)
                        .and(w -> w.eq(User::getEmail, keyword).or().eq(User::getPhone, keyword)));
        return users.stream().map(this::toUserInfoResponse).collect(Collectors.toList());
    }

    // 发送好友申请：校验不能加自己、不能重复申请、已是好友则拒绝
    @Override
    public void sendApply(Long fromUserId, Long toUserId) {
        if (fromUserId.equals(toUserId)) {
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR, "不能向自己发送好友申请");
        }

        User target = userMapper.selectById(toUserId);
        if (target == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "目标用户不存在");
        }

        if (isFriend(fromUserId, toUserId)) {
            throw new BusinessException(ResultCodeEnum.CONFLICT, "已经是好友");
        }

        // 检查是否已有待处理的申请
        Long existCount = friendApplyMapper.selectCount(
                new LambdaQueryWrapper<FriendApply>()
                        .eq(FriendApply::getFromUserId, fromUserId)
                        .eq(FriendApply::getToUserId, toUserId)
                        .eq(FriendApply::getStatus, Constants.APPLY_PENDING));
        if (existCount > 0) {
            throw new BusinessException(ResultCodeEnum.CONFLICT, "已发送过申请，请等待对方处理");
        }

        FriendApply apply = new FriendApply();
        apply.setFromUserId(fromUserId);
        apply.setToUserId(toUserId);
        apply.setStatus(Constants.APPLY_PENDING);
        friendApplyMapper.insert(apply);
        log.info("好友申请发送: from={}, to={}", fromUserId, toUserId);
    }

    // 处理好友申请：同意后双向建立好友关系，放入默认分组
    @Override
    @Transactional
    public void handleApply(Long userId, Long applyId, FriendApplyHandleRequest request) {
        FriendApply apply = friendApplyMapper.selectById(applyId);
        if (apply == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "申请不存在");
        }
        if (!apply.getToUserId().equals(userId)) {
            throw new BusinessException(ResultCodeEnum.FORBIDDEN, "无权处理此申请");
        }
        if (apply.getStatus() != Constants.APPLY_PENDING) {
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR, "申请已处理");
        }

        // 更新申请状态
        FriendApply updateApply = new FriendApply();
        updateApply.setId(applyId);
        updateApply.setStatus(request.getStatus());
        updateApply.setHandleTime(java.time.LocalDateTime.now());
        friendApplyMapper.updateById(updateApply);

        // 同意申请：双向建立好友关系
        if (request.getStatus() == Constants.APPLY_ACCEPT) {
            FriendGroup defaultGroup = getOrCreateDefaultGroup(userId);
            FriendGroup defaultGroupFrom = getOrCreateDefaultGroup(apply.getFromUserId());

            Friend f1 = new Friend();
            f1.setUserId(userId);
            f1.setFriendId(apply.getFromUserId());
            f1.setGroupId(defaultGroup.getId());
            friendMapper.insert(f1);

            Friend f2 = new Friend();
            f2.setUserId(apply.getFromUserId());
            f2.setFriendId(userId);
            f2.setGroupId(defaultGroupFrom.getId());
            friendMapper.insert(f2);

            log.info("好友关系建立: user1={}, user2={}", userId, apply.getFromUserId());
        }
    }

    // 获取收到的好友申请列表
    @Override
    public List<FriendApplyVO> getApplyList(Long userId) {
        List<FriendApply> applies = friendApplyMapper.selectList(
                new LambdaQueryWrapper<FriendApply>()
                        .eq(FriendApply::getToUserId, userId)
                        .orderByDesc(FriendApply::getApplyTime));

        return applies.stream().map(a -> {
            FriendApplyVO vo = new FriendApplyVO();
            vo.setId(a.getId());
            vo.setFromUserId(a.getFromUserId());
            vo.setStatus(a.getStatus());
            vo.setApplyTime(a.getApplyTime());

            // 填充申请人信息
            User fromUser = userMapper.selectById(a.getFromUserId());
            if (fromUser != null) {
                vo.setFromUsername(fromUser.getUsername());
                vo.setFromAvatar(fromUser.getAvatar());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    // 获取好友列表：支持按分组筛选
    @Override
    public List<FriendVO> getFriendList(Long userId, Long groupId) {
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<Friend>()
                .eq(Friend::getUserId, userId);
        if (groupId != null) {
            wrapper.eq(Friend::getGroupId, groupId);
        }

        List<Friend> friends = friendMapper.selectList(wrapper);
        return friends.stream().map(f -> {
            FriendVO vo = new FriendVO();
            vo.setFriendId(f.getFriendId());
            vo.setGroupId(f.getGroupId());

            // 填充好友用户信息
            User friendUser = userMapper.selectById(f.getFriendId());
            if (friendUser != null) {
                vo.setUsername(friendUser.getUsername());
                vo.setNickname(friendUser.getNickname());
                vo.setAvatar(friendUser.getAvatar());
            }

            // 填充分组名称
            if (f.getGroupId() != null) {
                FriendGroup group = friendGroupMapper.selectById(f.getGroupId());
                if (group != null) {
                    vo.setGroupName(group.getGroupName());
                }
            }
            return vo;
        }).collect(Collectors.toList());
    }

    // 创建好友分组
    @Override
    public void createGroup(Long userId, FriendGroupCreateRequest request) {
        FriendGroup group = new FriendGroup();
        group.setUserId(userId);
        group.setGroupName(request.getGroupName());
        friendGroupMapper.insert(group);
        log.info("好友分组创建: userId={}, groupName={}", userId, request.getGroupName());
    }

    // 移动好友到指定分组
    @Override
    public void moveFriendToGroup(Long userId, Long friendId, Long groupId) {
        Friend friend = friendMapper.selectOne(
                new LambdaQueryWrapper<Friend>()
                        .eq(Friend::getUserId, userId)
                        .eq(Friend::getFriendId, friendId));
        if (friend == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "好友关系不存在");
        }

        // 校验分组归属当前用户
        FriendGroup group = friendGroupMapper.selectById(groupId);
        if (group == null || !group.getUserId().equals(userId)) {
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR, "分组不存在");
        }

        Friend update = new Friend();
        update.setId(friend.getId());
        update.setGroupId(groupId);
        friendMapper.updateById(update);
    }

    // 判断两人是否为好友
    @Override
    public boolean isFriend(Long userId, Long targetUserId) {
        return friendMapper.selectCount(
                new LambdaQueryWrapper<Friend>()
                        .eq(Friend::getUserId, userId)
                        .eq(Friend::getFriendId, targetUserId)) > 0;
    }

    // 获取或创建默认分组
    private FriendGroup getOrCreateDefaultGroup(Long userId) {
        FriendGroup group = friendGroupMapper.selectOne(
                new LambdaQueryWrapper<FriendGroup>()
                        .eq(FriendGroup::getUserId, userId)
                        .eq(FriendGroup::getGroupName, "默认"));
        if (group == null) {
            group = new FriendGroup();
            group.setUserId(userId);
            group.setGroupName("默认");
            friendGroupMapper.insert(group);
        }
        return group;
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
        return resp;
    }
}
