package com.littlewin.note.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminNoteVO {

    private Long noteId;

    private String title;

    private String summary;

    private Integer status;

    private Integer isPublic;

    private Integer viewCount;

    private Integer likeCount;

    private Integer commentCount;

    private Long userId;

    private String author;

    private String avatar;

    private Long categoryId;

    private String categoryName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
