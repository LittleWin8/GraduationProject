package com.littlewin.note.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("ai_user_quota")
public class AiUserQuota implements Serializable {

    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;

    private Integer monthlyTokenLimit;

    private Integer monthlyRequestLimit;

    private Integer usedTokens;

    private Integer usedRequests;

    private LocalDate quotaResetDate;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
