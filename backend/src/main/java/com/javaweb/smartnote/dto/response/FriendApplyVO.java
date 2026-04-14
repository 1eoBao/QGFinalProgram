package com.javaweb.smartnote.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FriendApplyVO {
    private Long id;
    private Long fromUserId;
    private String fromUsername;
    private String fromAvatar;
    private Integer status;
    private LocalDateTime applyTime;
}
