package com.littlewin.note.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyNoteVO {

    private Long noteId;

    private String title;

    private LocalDateTime updateTime;

    private Integer viewCount;

    private Integer isPublic;
}