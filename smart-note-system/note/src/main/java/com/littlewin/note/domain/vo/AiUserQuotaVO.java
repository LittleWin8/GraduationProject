package com.littlewin.note.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiUserQuotaVO {

    private Long userId;

    private String userName;

    private Integer monthlyTokenLimit;

    private Integer monthlyRequestLimit;

    private Integer usedTokens;

    private Integer usedRequests;

    private LocalDate quotaResetDate;
}
