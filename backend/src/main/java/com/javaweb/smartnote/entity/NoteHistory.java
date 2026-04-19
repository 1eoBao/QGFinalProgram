package com.javaweb.smartnote.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("note_history")
public class NoteHistory {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long noteId;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime viewTime;
}
