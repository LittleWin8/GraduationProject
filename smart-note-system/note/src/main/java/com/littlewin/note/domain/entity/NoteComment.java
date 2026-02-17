package com.littlewin.note.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("note_comment")
public class NoteComment implements Serializable {

    @TableId(value = "comment_id", type = IdType.AUTO)
    private Long commentId;

    private Long noteId;

    private Long userId;

    private String content;

    private Long parentId;

    private LocalDateTime createTime;

    @TableLogic
    private Integer delFlag;
}
