package com.littlewin.note.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("note_tag_rel")
public class NoteTagRel implements Serializable {

    @TableId(value = "note_id", type = IdType.INPUT)
    private Long noteId;

    @TableField("tag_id")
    private Long tagId;
}
