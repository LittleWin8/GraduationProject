package com.littlewin.note.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 笔记创建请求体
 */
@Data
public class NoteCreateDTO {

    /** 笔记标题（必填） */
    private String title;

    /** 笔记内容（必填） */
    private String content;

    /** 分类ID（可选） */
    private Long categoryId;

    /** 是否公开：1-公开，0-私密（默认1） */
    private Integer isPublic = 1;

    /** 标签ID列表（可选） */
    private List<Long> tagIds;
}
