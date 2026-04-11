package com.littlewin.system.service;

import com.littlewin.system.domain.vo.MenuVO;

import java.util.List;
import java.util.Map;

public interface AdminAuthService {

    // 登录验证，仅返回生成的 Token
    String login(String username, String password);

    // 退出登录
    void logout();

    // 拆分后的新方法
    List<MenuVO> getAuthMenuList();

    Map<String, List<String>> getAuthButtonList();
}
