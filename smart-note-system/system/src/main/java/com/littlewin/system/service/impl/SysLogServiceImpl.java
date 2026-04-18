package com.littlewin.system.service.impl;

import com.littlewin.common.enums.LogAction;
import com.littlewin.common.enums.LogStatus;
import com.littlewin.common.utils.ServletUtils;
import com.littlewin.system.domain.dto.AdminLoginDTO;
import com.littlewin.system.domain.entity.SysLogOperation;
import com.littlewin.system.mapper.SysLogOperationMapper;
import com.littlewin.system.service.SysLogService;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SysLogServiceImpl implements SysLogService {

    @Resource
    private SysLogOperationMapper sysLogOperationMapper;

    @Override
    public void recordAuthLog(AdminLoginDTO user, LogStatus status, LogAction action, String desc, String errorMsg) {
        // 认证模块 businessId 通常就是 userId
        Long businessId = (user != null) ? user.getUserId() : null;
        recordOperation(user, businessId, "AUTH", action, status, desc, errorMsg);
    }

    @Override
    public void recordAiLog(AdminLoginDTO user, Long businessId, LogAction action, String desc, String errorMsg) {
        recordOperation(user, businessId, "AI", action, LogStatus.SUCCESS, desc, errorMsg);
    }

    @Override
    public void recordDictLog(AdminLoginDTO user, Long businessId, LogAction action, LogStatus status, String desc, String errorMsg) {
        recordOperation(user, businessId, "DICT", action, status, desc, errorMsg);
    }

    @Override
    public void recordUserLog(AdminLoginDTO user, Long businessId, LogAction action, LogStatus status, String desc, String errorMsg) {
        recordOperation(user, businessId, "USER", action, status, desc, errorMsg);
    }

    @Override
    public void recordRoleLog(AdminLoginDTO user, Long businessId, LogAction action, LogStatus status, String desc, String errorMsg) {
        recordOperation(user, businessId, "ROLE", action, status, desc, errorMsg);
    }

    @Async("layoutThreadPool") // 配置专门的线程池执行异步任务
    @Override
    public void recordOperation(AdminLoginDTO user, Long businessId, String module, LogAction action, LogStatus status, String desc, String errorMsg) {
        SysLogOperation log = new SysLogOperation();

        // 填充用户信息
        if (user != null) {
            log.setUserId(user.getUserId());
            log.setUsername(user.getUsername());
        } else {
            log.setUsername("Unknown");
        }

        // 填充业务属性
        log.setModule(module);
        log.setBusinessId(businessId);
        log.setActionType(action.getCode());
        log.setStatus(status.getCode());
        log.setDescription(desc);
        log.setErrorMsg(errorMsg);

        // 填充环境信息
        log.setIpAddress(ServletUtils.getClientIp());
        var request = ServletUtils.getRequest();
        if (request != null) {
            log.setRequestUrl(request.getRequestURI());
            log.setRequestMethod(request.getMethod());
        }

        sysLogOperationMapper.insertOperationLog(log);
    }
}