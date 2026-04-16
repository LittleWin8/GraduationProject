package com.littlewin.system.service;

import com.littlewin.system.domain.dto.AdminLoginDTO;
import com.littlewin.common.enums.LogAction;
import com.littlewin.common.enums.LogStatus;

public interface SysLogService {
    /** 记录认证模块日志 */
    void recordAuthLog(AdminLoginDTO user, LogStatus status, LogAction action, String desc, String errorMsg);

    /** 记录 AI 模块日志 */
    void recordAiLog(AdminLoginDTO user, Long businessId, LogAction action, String desc, String errorMsg);

    /**
     * 记录字典模块操作日志
     */
    void recordDictLog(AdminLoginDTO user, Long businessId, LogAction action, LogStatus status, String desc, String errorMsg);

    /**
     * 记录用户模块操作日志
     */
    void recordUserLog(AdminLoginDTO user, Long businessId, LogAction action, LogStatus status, String desc, String errorMsg);

    /** 通用记录方法（供内部或特殊场景使用） */
    void recordOperation(AdminLoginDTO user, Long businessId, String module, LogAction action, LogStatus status, String desc, String errorMsg);
}