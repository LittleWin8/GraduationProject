package com.littlewin.system.service.impl;

import com.littlewin.common.exception.ServiceException;
import com.littlewin.common.utils.JwtUtils;
import com.littlewin.system.domain.dto.AdminLoginDTO;
import com.littlewin.system.domain.entity.SysLogOperation;
import com.littlewin.common.utils.ServletUtils;
import com.littlewin.system.mapper.SysLogOperationMapper;
import com.littlewin.system.mapper.UserAuthMapper;
import com.littlewin.system.service.AdminAuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;

@Service
public class AdminAuthServiceImpl implements AdminAuthService {

    @Resource
    private UserAuthMapper userAuthMapper;

    @Resource
    private SysLogOperationMapper sysLogOperationMapper;

    // 注入 Spring Security 的认证管理器
    private final AuthenticationManager authenticationManager;

    public AdminAuthServiceImpl(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public String login(String username, String password) {
        // 提前查出用户信息，用于日志记录（即便认证失败也能拿到用户ID）
        AdminLoginDTO loginUser = userAuthMapper.selectAdminLoginUser(username);

        try {
            // 1. 构建认证 Token (未认证状态)
            // 将前端传来的用户名和密码封装起来
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(username, password);

            // 2. 执行认证
            // 这一行代码会触发 Spring Security 的流程：
            // a. 调用上面的 UserDetailsServiceImpl 加载用户
            // b. 自动比对密码
            // c. 如果失败，这里直接抛异常
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            // 3. 认证通过，获取用户ID
            // authentication.getPrincipal() 通常是 UserDetails 对象
            // authentication.getName() 通常是用户名
            String userId = authentication.getName();

            // 记录成功登录日志
            recordLog(loginUser, 1, 1, "登录成功", null);

            // 4. 生成 JWT Token
            return JwtUtils.createToken(userId);

        } catch (AuthenticationException e) {
            // 5. 捕获认证异常
            // Spring Security 的认证失败（用户不存在、密码错误、禁用等）都会走到这里
            // 记录失败日志：action类型为 1，状态为 0 (失败)
            recordLog(loginUser, 0, 1, "登录失败", e.getMessage());
            throw new ServiceException("登录失败，用户名或密码错误");
        }
    }

    @Override
    public void logout() {
        // 从 Security 上下文中获取当前认证的用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            // 1. 根据用户名查出用户ID，用于记录日志
            AdminLoginDTO loginUser = userAuthMapper.selectAdminLoginUser(auth.getName());

            // 2. 记录退出日志：action类型为 2 (退出)
            recordLog(loginUser, 1, 2, "用户退出登录", null);
        }
        // 3. 清除上下文
        SecurityContextHolder.clearContext();
    }

    /**
     * 记录认证相关操作日志（登录/退出）
     *
     * @param user     用户信息
     * @param status   状态 (1成功, 0失败)
     * @param action   行为类型 (1登录, 2退出)
     * @param desc     操作描述
     * @param errorMsg 错误信息
     */
    private void recordLog(AdminLoginDTO user, Integer status, Integer action, String desc, String errorMsg) {
        SysLogOperation log = new SysLogOperation();

        // 1. 设置主体信息
        if (user != null) {
            log.setUserId(user.getUserId());
            log.setUsername(user.getUsername());
            log.setBusinessId(user.getUserId()); // 对于 AUTH 模块，业务ID关联用户ID
        } else {
            log.setUsername("未知用户");
        }

        // 2. 设置模块属性
        log.setModule("AUTH");          // 固定为认证模块
        log.setActionType(action);      // 1-登录, 2-退出
        log.setStatus(status);          // 1-成功, 0-失败
        log.setDescription(desc);
        log.setErrorMsg(errorMsg);

        // 3. 获取环境信息（利用你刚实现的 ServletUtils）
        log.setIpAddress(ServletUtils.getClientIp());

        HttpServletRequest request = ServletUtils.getRequest();
        if (request != null) {
            log.setRequestUrl(request.getRequestURI());
            log.setRequestMethod(request.getMethod());
        }

        // 4. 执行持久化
        sysLogOperationMapper.insertOperationLog(log);
    }
}