package com.javaweb.smartnote.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class NoteListResponse {
    private Long id;
    private String title;
    private String contentPreview;
    private Integer permissionType;
    private Boolean canEdit;
    private LocalDateTime lastEditTime;
    private LocalDateTime createTime;
    private List<TagVO> tags;

    @Data
    public static class TagVO {
        private Long id;
        private String name;
    }
}
