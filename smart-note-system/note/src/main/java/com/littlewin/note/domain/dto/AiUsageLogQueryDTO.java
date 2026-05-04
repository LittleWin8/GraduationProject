package com.littlewin.note.domain.dto;

import lombok.Data;

@Data
public class AiUsageLogQueryDTO {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private Long userId;

    private Integer status;

    private String actionType;

    private String startTime;

    private String endTime;
}
