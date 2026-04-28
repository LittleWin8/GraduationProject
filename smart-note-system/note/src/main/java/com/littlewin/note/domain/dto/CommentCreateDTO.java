package com.littlewin.note.domain.dto;

import lombok.Data;

/**
 * 发表评论请求体
 */
@Data
public class CommentCreateDTO {

    private Long noteId;

    private String content;

    private Long parentId;
}
