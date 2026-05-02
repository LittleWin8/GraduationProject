package com.littlewin.common.exception;

import com.littlewin.common.core.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常（自定义）
     */
    @ExceptionHandler(ServiceException.class)
    public Result<?> handleServiceException(ServiceException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.build(e.getCode(), e.getMessage(), null);
    }

    /**
     * @Valid 校验失败（请求体参数校验）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("参数校验失败");
        log.warn("参数校验失败: {}", message);
        return Result.build(400, message, null);
    }

    /**
     * 路径参数/查询参数校验失败
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .findFirst()
                .orElse("参数校验失败");
        log.warn("约束违反: {}", message);
        return Result.build(400, message, null);
    }

    /**
     * 权限不足（403）
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Result<?> handleAccessDenied(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.build(403, "权限不足", null);
    }

    /**
     * 未认证（401）
     * 注意：Spring Security 的 JWT 过滤器链有自己的 401 处理逻辑（JwtAuthenticationFilter 中 clearContext 后由框架返回 401），
     * 此处理器作为额外保障，处理非标准流程中的认证异常。
     */
    @ExceptionHandler(AuthenticationException.class)
    public Result<?> handleAuthentication(AuthenticationException e) {
        log.warn("未认证: {}", e.getMessage());
        return Result.build(401, "未登录或Token已过期", null);
    }

    /**
     * 请求体解析失败（JSON 格式错误、类型不匹配）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<?> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        return Result.build(400, "请求参数格式错误", null);
    }

    /**
     * 兜底异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.build(500, "系统异常", null);
    }
}
