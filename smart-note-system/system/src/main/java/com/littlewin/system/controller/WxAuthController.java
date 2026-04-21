package com.littlewin.system.controller;

import com.littlewin.common.core.Result;
import com.littlewin.common.enums.LogAction;
import com.littlewin.common.enums.LogModule;
import com.littlewin.common.log.annotation.Log;
import com.littlewin.system.service.WxUserService;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/wx/auth")
public class WxAuthController {

    @Resource
    private WxUserService wxUserService;

    @PostMapping("/login")
    @Log(module = LogModule.AUTH, action = LogAction.LOGIN, desc = "微信小程序登录")
    public Result login(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        String nickName = body.get("nickName");
        String avatarUrl = body.get("avatarUrl");

        if (code == null) return Result.error("code 不能为空");

        String token = wxUserService.login(code, nickName, avatarUrl);

        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        return Result.success(data);
    }
}