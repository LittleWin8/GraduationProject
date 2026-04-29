package com.littlewin.note.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminNoteQueryDTO {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private Integer status;

    private Long categoryId;

    private String keyword;

    private Long userId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
