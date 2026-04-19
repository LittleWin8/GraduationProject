package com.littlewin.common.log.aspect;


import com.littlewin.common.log.annotation.Log;
import com.littlewin.common.log.context.LogContext;
import com.littlewin.common.core.AdminLoginDTO;
import com.littlewin.common.log.entity.SysLogOperation;
import com.littlewin.common.log.manager.LogAsyncManager;
import com.littlewin.common.utils.SecurityUtils;
import com.littlewin.common.utils.ServletUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Aspect
@Component
public class LogAspect {
    @Resource
    private LogAsyncManager logAsyncManager;

    @Around("@annotation(controllerLog)")
    public Object around(ProceedingJoinPoint joinPoint, Log controllerLog) throws Throwable {
        Object result;
        try {
            result = joinPoint.proceed(); // 执行业务
            handleLog(controllerLog, null); // 成功：errorMsg为空，status为1
            return result;
        } catch (Exception e) {
            handleLog(controllerLog, e); // 失败：填充errorMsg，status为0
            throw e;
        } finally {
            LogContext.clear(); // 必做：清空ThreadLocal
        }
    }

    private void handleLog(Log controllerLog, Exception e) {
        SysLogOperation log = new SysLogOperation();

        // 1. 状态判断
        if (e == null) {
            log.setStatus(1); // 成功
            log.setErrorMsg(null);
        } else {
            log.setStatus(0); // 失败
            log.setErrorMsg(e.getMessage());
        }

        // 2. 描述与ID优先级：Context(动态) > Annotation(静态)
        String dynamicDesc = LogContext.getDesc();
        log.setDescription(dynamicDesc != null ? dynamicDesc : controllerLog.desc());
        log.setBusinessId(LogContext.getBusinessId());

        // 3. 填充固定字段
        log.setModule(controllerLog.module().getModule());
        log.setActionType(controllerLog.action().getCode());

        // 4. 环境信息 (IP, URL, Method)
        log.setIpAddress(ServletUtils.getClientIp());
        HttpServletRequest request = ServletUtils.getRequest();
        if (request != null) {
            log.setRequestUrl(request.getRequestURI());
            log.setRequestMethod(request.getMethod());
        }

        // 5. 用户信息 (从你的SecurityUtils拿)
        AdminLoginDTO user = SecurityUtils.getLoginUser();
        if (user != null) {
            log.setUserId(user.getUserId());
            log.setUsername(user.getUsername());
        }

        // 6. 设置发生时间
        log.setCreateTime(LocalDateTime.now());

        // 7. 异步入库
        logAsyncManager.saveLog(log);
    }
}