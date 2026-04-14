package com.javaweb.smartnote.dto.request;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String nickname;
    private String avatar;
    private String motto;
}
