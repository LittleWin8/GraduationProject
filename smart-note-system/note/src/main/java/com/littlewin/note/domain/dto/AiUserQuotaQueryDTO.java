package com.littlewin.note.domain.dto;

import lombok.Data;

@Data
public class AiUserQuotaQueryDTO {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private String keyword;
}
