package com.littlewin.system.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 操作日志查询参数
 */
@Data
public class OperationLogQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer pageNum = 1;
    private Integer pageSize = 10;

    /** 模块：AUTH/USER/NOTE/DICT/AI/ROLE */
    private String module;

    /** 操作类型：1登录/2退出/3创建/4修改/5删除 */
    private Integer actionType;

    /** 用户名（模糊） */
    private String username;

    /** 状态：0失败/1成功 */
    private Integer status;

    /** 开始时间（yyyy-MM-dd） */
    private String startTime;

    /** 结束时间（yyyy-MM-dd） */
    private String endTime;
}
