package com.littlewin.system.controller;

import com.littlewin.common.constants.Constants;
import com.littlewin.common.core.Result;
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

    @PostMapping("/logout")
    public Result logout() {
        adminAuthService.logout();
        return Result.success("退出成功");
    }

    /**
     * 获取菜单权限列表 (树形结构)
     */
    @GetMapping("/getAuthMenuList")
    public Result getAuthMenuList() {
        return Result.success(adminAuthService.getAuthMenuList());
    }

    /**
     * 获取按钮权限列表 (Map结构：页面name -> 权限数组)
     */
    @GetMapping("/getAuthButtonList")
    public Result getAuthButtonList() {
        return Result.success(adminAuthService.getAuthButtonList());
    }


}