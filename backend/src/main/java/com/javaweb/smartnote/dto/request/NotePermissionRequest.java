package com.javaweb.smartnote.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class NotePermissionRequest {
    @NotNull(message = "权限类型不能为空")
    private Integer permissionType;

    private List<Long> targetUserIds;
}
