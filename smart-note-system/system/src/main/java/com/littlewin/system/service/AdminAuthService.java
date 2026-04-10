package com.littlewin.system.service;

import com.littlewin.system.domain.dto.AdminLoginResponseDTO;

public interface AdminAuthService {

    // 登录验证，仅返回生成的 Token
    String login(String username, String password);

    // 获取当前登录用户的权限数据（菜单树+按钮权限）
    AdminLoginResponseDTO getLoginUserData();

    // 退出登录
    void logout();
}
