package com.littlewin.note.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("ai_usage_log")
public class AiUsageLog implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long noteId;

    private String actionType;

    private Integer promptTokens;

    private Integer completionTokens;

    private Integer totalTokens;

    private String modelName;

    private Integer status;

    private String errorMsg;

    private LocalDateTime createTime;
}
