package com.javaweb.smartnote.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ai_analysis")
public class AiAnalysis {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long noteId;
    private Long userId;
    private String summary;
    private String keyPoints;
    private String suggestedTags;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime analysisTime;
}
