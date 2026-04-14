package com.javaweb.smartnote.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class NoteDetailResponse {
    private Long id;
    private String title;
    private String content;
    private Integer permissionType;
    private LocalDateTime lastEditTime;
    private LocalDateTime createTime;
    private List<TagVO> tags;
    private AiAnalysisVO aiAnalysis;
    private Boolean canEdit;
    private Boolean isOwner;
    private List<Long> targetUserIds;

    @Data
    public static class TagVO {
        private Long id;
        private String name;
    }

    @Data
    public static class AiAnalysisVO {
        private Long id;
        private String summary;
        private String keyPoints;
        private String suggestedTags;
        private LocalDateTime analysisTime;
    }
}
