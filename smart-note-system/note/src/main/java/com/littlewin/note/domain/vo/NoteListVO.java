package com.littlewin.note.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 笔记列表项视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteListVO {

    /** 笔记ID */
    private Long noteId;

    /** 笔记标题 */
    private String title;

    /** 笔记摘要（content截取前200字） */
    private String summary;

    /** 浏览量 */
    private Integer viewCount;

    /** 点赞数 */
    private Integer likeCount;

    /** 评论数 */
    private Integer commentCount;

    /** 是否公开：1-公开，0-私密 */
    private Integer isPublic;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /** 作者昵称 */
    private String author;

    /** 作者头像 */
    private String avatar;

    /** 分类名称 */
    private String categoryName;

    /** 标签列表 */
    private List<TagItem> tags;

    /**
     * 标签简要信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagItem {
        /** 标签ID */
        private Long tagId;
        /** 标签名称 */
        private String tagName;
    }
}
