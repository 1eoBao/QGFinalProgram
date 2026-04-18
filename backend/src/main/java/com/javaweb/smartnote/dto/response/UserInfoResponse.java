package com.javaweb.smartnote.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserInfoResponse {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String nickname;
    private String avatar;
    private String motto;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
