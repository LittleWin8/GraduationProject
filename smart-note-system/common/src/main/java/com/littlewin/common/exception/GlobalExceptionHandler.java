package com.littlewin.common.exception;

import com.littlewin.common.core.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(ServiceException.class)
    public Result<?> handleServiceException(ServiceException e) {
        return Result.error(e.getMessage());
    }

    /**
     * 兜底异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        e.printStackTrace(); // ⚠️ 开发阶段保留，生产建议用日志
        return Result.error("系统异常");
    }
}