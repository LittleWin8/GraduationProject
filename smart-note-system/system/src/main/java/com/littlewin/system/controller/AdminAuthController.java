package com.littlewin.system.controller;

import com.littlewin.common.constants.Constants;
import com.littlewin.common.core.Result;
import com.littlewin.system.domain.dto.AdminLoginResponseDTO;
import com.littlewin.system.service.AdminAuthService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    @Resource
    private AdminAuthService adminAuthService;

    /**
     * 登录接口：仅返回 Token
     */
    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> body) {
        String token = adminAuthService.login(
                body.get("username"),
                body.get("password")
        );
        Map<String, String> data = new HashMap<>();
        data.put("token", Constants.TOKEN_PREFIX + token);
        return Result.success(data);
    }

    /**
     * 权限数据接口：根据请求头中的 Token 获取菜单和按钮
     */
    @GetMapping("/getAuthData")
    public Result getAuthData() {
        AdminLoginResponseDTO data = adminAuthService.getLoginUserData();
        return Result.success(data);
    }

    @PostMapping("/logout")
    public Result logout() {
        adminAuthService.logout();
        return Result.success("退出成功");
    }
}