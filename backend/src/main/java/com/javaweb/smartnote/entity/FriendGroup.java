package com.javaweb.smartnote.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("friend_group")
public class FriendGroup {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String groupName;
}
