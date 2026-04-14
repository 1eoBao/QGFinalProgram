package com.javaweb.smartnote.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("note_permission")
public class NotePermission {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long noteId;
    private Long targetUserId;
    private Integer permType;
}
