package com.javaweb.smartnote.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FriendVO {
    private Long friendId;
    private String username;
    private String nickname;
    private String avatar;
    private Long groupId;
    private String groupName;
}
