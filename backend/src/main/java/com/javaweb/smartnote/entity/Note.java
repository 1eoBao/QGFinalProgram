package com.javaweb.smartnote.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("note")
public class Note {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String title;
    private String content;
    private Integer permissionType;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime lastEditTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
