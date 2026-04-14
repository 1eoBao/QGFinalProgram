package com.javaweb.smartnote.service;

import com.javaweb.smartnote.dto.request.FriendApplyHandleRequest;
import com.javaweb.smartnote.dto.request.FriendGroupCreateRequest;
import com.javaweb.smartnote.dto.response.FriendApplyVO;
import com.javaweb.smartnote.dto.response.FriendVO;
import com.javaweb.smartnote.dto.response.UserInfoResponse;

import java.util.List;

public interface FriendService {

    List<UserInfoResponse> searchUser(String keyword, Long currentUserId);

    void sendApply(Long fromUserId, Long toUserId);

    void handleApply(Long userId, Long applyId, FriendApplyHandleRequest request);

    List<FriendApplyVO> getApplyList(Long userId);

    List<FriendVO> getFriendList(Long userId, Long groupId);

    void createGroup(Long userId, FriendGroupCreateRequest request);

    void moveFriendToGroup(Long userId, Long friendId, Long groupId);

    boolean isFriend(Long userId, Long targetUserId);
}
