package com.javaweb.smartnote.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("friend_apply")
public class FriendApply {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long fromUserId;
    private Long toUserId;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime applyTime;

    private LocalDateTime handleTime;
}
