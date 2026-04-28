package com.littlewin.note.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 评论返回VO（含作者信息）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentVO {

    private Long commentId;

    private Long noteId;

    private String content;

    private Long parentId;

    private LocalDateTime createTime;

    private String author;

    private String avatar;

    private Boolean isOwner;
}
