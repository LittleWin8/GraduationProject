package com.littlewin.note.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("note_attachment")
public class NoteAttachment implements Serializable {

    @TableId(value = "attach_id", type = IdType.AUTO)
    private Long attachId;

    private Long noteId;

    private Long userId;

    private String fileUrl;

    private String fileName;

    private String fileSuffix;

    private Long fileSize;

    private LocalDateTime createTime;
}