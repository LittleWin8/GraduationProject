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

    /**
     * 非数据库字段：用于展示该标签下的笔记数量
     */
    @TableField(exist = false)
    private Integer noteCount;
}