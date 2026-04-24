package com.littlewin.note.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("note_tag")
public class NoteTag implements Serializable {

    @TableId(value = "tag_id", type = IdType.AUTO)
    private Long tagId;

    private String name;

    private Long userId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}