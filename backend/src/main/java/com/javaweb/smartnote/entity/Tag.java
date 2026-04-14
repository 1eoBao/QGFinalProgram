package com.javaweb.smartnote.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("tag")
public class Tag {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
}
