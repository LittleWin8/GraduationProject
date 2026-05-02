package com.littlewin.note.domain.dto;

import lombok.Data;

@Data
public class AdminTagQueryDTO {

    private Integer pageNum = 1;

    private Integer pageSize = 20;

    private String keyword;
}
