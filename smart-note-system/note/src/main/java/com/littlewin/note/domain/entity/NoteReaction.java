package com.littlewin.note.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("note_reaction")
public class NoteReaction implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long noteId;

    private Long userId;

    private Integer attitude;

    private Integer isFavorite;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
