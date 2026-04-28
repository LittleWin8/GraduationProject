package com.littlewin.note.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class NoteDetailVO {
    private Long noteId;
    private Long userId;
    private String title;
    private String content;
    private String author;
    private String avatar;
    private Long categoryId;
    private Integer isPublic;
    private Integer isLiked;
    private Integer likes;
    private Integer comments;
    private List<Long> tagIds;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
