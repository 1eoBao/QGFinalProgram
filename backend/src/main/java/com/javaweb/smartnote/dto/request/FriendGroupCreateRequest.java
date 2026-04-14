package com.javaweb.smartnote.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FriendGroupCreateRequest {
    @NotBlank(message = "分组名称不能为空")
    private String groupName;
}
