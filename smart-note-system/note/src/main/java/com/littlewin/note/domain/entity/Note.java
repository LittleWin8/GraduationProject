package com.littlewin.note.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("note")
public class Note implements Serializable {

    @TableId(value = "note_id", type = IdType.AUTO)
    private Long noteId;

    private Long userId;

    private String categoryName;

    private String title;

    private String content;

    private Integer isPublic;

    private Integer status;

    private Integer viewCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
