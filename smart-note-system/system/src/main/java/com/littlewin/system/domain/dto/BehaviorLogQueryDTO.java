package com.littlewin.system.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 行为日志查询参数
 */
@Data
public class BehaviorLogQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer pageNum = 1;
    private Integer pageSize = 10;

    /** 行为类型：1浏览/2搜索 */
    private Integer actionType;

    /** 用户ID */
    private Long userId;

    /** 开始时间（yyyy-MM-dd） */
    private String startTime;

    /** 结束时间（yyyy-MM-dd） */
    private String endTime;
}
