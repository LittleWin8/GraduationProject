package com.littlewin.system.controller;

import com.littlewin.common.constants.Constants;
import com.littlewin.common.core.Result;
import com.littlewin.common.enums.LogAction;
import com.littlewin.common.enums.LogModule;
import com.littlewin.common.log.annotation.Log;
import com.littlewin.common.log.context.LogContext;
import com.littlewin.common.utils.JwtUtils;
import com.littlewin.common.utils.SecurityUtils;
import com.littlewin.system.service.WxUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/wx/auth")
public class WxAuthController {

    private static final String REDIS_KEY_PREFIX = "token:blacklist:";

    @Resource
    private WxUserService wxUserService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @PostMapping("/login")
    @Log(module = LogModule.AUTH, action = LogAction.LOGIN, desc = "微信小程序登录")
    public Result login(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        String nickName = body.get("nickName");
        String avatarUrl = body.get("avatarUrl");

        if (code == null) return Result.error("code 不能为空");
        Map<String, Object> loginData = wxUserService.login(code, nickName, avatarUrl);

        return Result.success(loginData);
    }

    @PostMapping("/logout")
    @Log(module = LogModule.AUTH, action = LogAction.LOGOUT, desc = "退出登录")
    public Result logout(HttpServletRequest request) {
        LogContext.setBusinessId(SecurityUtils.getLoginUser().getUserId());

        // 将 token 加入 Redis 黑名单，TTL 设为剩余有效期
        String header = request.getHeader(Constants.TOKEN_HEADER);
        if (header != null && header.startsWith(Constants.TOKEN_PREFIX)) {
            String token = header.substring(Constants.TOKEN_PREFIX.length());
            long remaining = JwtUtils.getRemainingExpiration(token);
            if (remaining > 0) {
                stringRedisTemplate.opsForValue()
                        .set(REDIS_KEY_PREFIX + token, "1", remaining, TimeUnit.MILLISECONDS);
            }
        }

        return Result.success("退出成功");
    }

    @GetMapping("/getUserInfo")
    public Result getUserInfo() {
        return Result.success(wxUserService.getUserInfo());
    }
}