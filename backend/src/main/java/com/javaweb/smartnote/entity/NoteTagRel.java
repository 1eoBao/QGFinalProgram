package com.javaweb.smartnote.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("note_tag_rel")
public class NoteTagRel {
    @TableField("note_id")
    private Long noteId;

    @TableField("tag_id")
    private Long tagId;
}
