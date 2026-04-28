package com.littlewin.note.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 消息返回VO（含发送者和笔记信息）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageVO {

    private Long id;

    private Integer type;

    private String content;

    private Boolean isRead;

    private LocalDateTime createTime;

    private Long noteId;

    private String noteTitle;

    private String senderName;

    private String senderAvatar;

    private Long commentId;
}
