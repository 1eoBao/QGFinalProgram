package com.javaweb.smartnote.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FriendApplyHandleRequest {
    @NotNull(message = "处理状态不能为空")
    private Integer status;
}
