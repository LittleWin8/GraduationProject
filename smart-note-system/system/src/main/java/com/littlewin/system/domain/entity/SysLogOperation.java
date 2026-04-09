package com.littlewin.system.domain.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysLogOperation {
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
    private LocalDateTime createTime;
}