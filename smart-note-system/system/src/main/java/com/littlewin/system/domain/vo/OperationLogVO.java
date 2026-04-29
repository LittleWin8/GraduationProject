package com.littlewin.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志列表 VO
 */
@Data
public class OperationLogVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String username;
    private String module;
    private Integer actionType;
    private Long businessId;
    private String description;
    private String requestUrl;
    private String requestMethod;
    private String ipAddress;
    private Integer status;
    private String errorMsg;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
