package com.littlewin.common.log.aspect;


import com.littlewin.common.enums.LogAction;
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
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Aspect
@Component
@Order(1)
public class LogAspect {
    @Resource
    private LogAsyncManager logAsyncManager;

    @Around("@annotation(controllerLog)")
    public Object around(ProceedingJoinPoint joinPoint, Log controllerLog) throws Throwable {
        AdminLoginDTO currentUser = null;
        try {
            currentUser = SecurityUtils.getLoginUser();
        } catch (Exception ignored) {}

        Object result;
        try {
            result = joinPoint.proceed(); // 执行业务
            handleLog(controllerLog, null, currentUser); // 成功：errorMsg为空，status为1
            return result;
        } catch (Exception e) {
            handleLog(controllerLog, e, currentUser); // 失败：填充errorMsg，status为0
            throw e;
        } finally {
            LogContext.clear(); // 必做：清空ThreadLocal
            if (controllerLog.action() == LogAction.LOGOUT) {
                SecurityContextHolder.clearContext();
            }
        }
    }

    private void handleLog(Log controllerLog, Exception e, AdminLoginDTO currentUser) {
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

        // 5. 用户信息获取逻辑优化
        Long userId = LogContext.getBusinessId();
        String username = LogContext.getUsername();

        // 如果 Context 里没有（比如非登录接口），再从 SecurityContext 获取
        if (userId == null || username == null) {
            AdminLoginDTO user = currentUser != null ? currentUser : SecurityUtils.getLoginUser();
            if (user != null) {
                userId = user.getUserId();
                username = user.getUsername();
            }
        }

        log.setUserId(userId);
        log.setUsername(username);

        // 6. 设置发生时间
        log.setCreateTime(LocalDateTime.now());

        // 7. 异步入库
        logAsyncManager.saveLog(log);
    }
}