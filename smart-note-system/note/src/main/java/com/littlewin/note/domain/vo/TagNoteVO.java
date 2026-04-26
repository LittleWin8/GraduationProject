package com.littlewin.note.domain.vo;

import lombok.Data;

@Data
public class TagNoteVO {
    private Long id;
    private String title;
    private String summary;
    private String author;
    private String avatar;
    private String type;
    private Integer isLiked;
    private Integer likes;
    private Integer comments;
}
