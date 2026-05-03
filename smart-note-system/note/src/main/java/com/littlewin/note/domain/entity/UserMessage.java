package com.littlewin.note.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 站内消息实体
 */
@Data
@TableName("user_message")
public class UserMessage implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long receiverId;

    private Long senderId;

    private String title;

    private Long noteId;

    private Long commentId;

    private Integer type;

    private String content;

    private Integer isRead;

    private LocalDateTime createTime;
}
