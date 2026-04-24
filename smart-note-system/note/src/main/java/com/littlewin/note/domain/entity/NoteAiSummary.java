package com.littlewin.note.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("note_ai_summary")
public class NoteAiSummary implements Serializable {

    @TableId(value = "note_id", type = IdType.INPUT)
    private Long noteId;

    private String summary;

    private String keywords;

    private String modelName;

    private LocalDateTime createTime;
}