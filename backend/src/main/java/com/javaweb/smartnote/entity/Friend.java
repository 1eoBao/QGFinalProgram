package com.javaweb.smartnote.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("friend")
public class Friend {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long friendId;
    private Long groupId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
