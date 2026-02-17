package com.littlewin.note.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("note_attachment")
public class NoteAttachment implements Serializable {

    @TableId(value = "attach_id", type = IdType.AUTO)
    private Long attachId;

    private Long noteId;

    private String fileUrl;

    private String fileName;

    private Long fileSize;
}
