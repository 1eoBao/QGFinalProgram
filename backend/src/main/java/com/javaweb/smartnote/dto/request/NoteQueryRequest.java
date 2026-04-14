package com.javaweb.smartnote.dto.request;

import lombok.Data;

@Data
public class NoteQueryRequest {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String keyword;
    private Integer permissionType;
}
