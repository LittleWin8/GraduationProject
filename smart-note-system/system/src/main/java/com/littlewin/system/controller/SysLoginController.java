package com.littlewin.system.controller;

import com.littlewin.common.core.AjaxResult;
import com.littlewin.system.domain.vo.LoginBody;
import com.littlewin.system.service.SysLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class SysLoginController {

    @Autowired
    private SysLoginService loginService;

    @PostMapping("admin/login")
    public AjaxResult login(@RequestBody LoginBody body) {

        String token =
                loginService.login(body.getUserId(), body.getPassword());

        return AjaxResult.success(token);
    }
}
