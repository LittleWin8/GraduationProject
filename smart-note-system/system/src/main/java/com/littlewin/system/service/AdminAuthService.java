package com.littlewin.system.service;

import com.littlewin.system.domain.vo.MenuVO;
import com.littlewin.system.domain.vo.UserInfoVO;

import java.util.List;
import java.util.Map;

public interface AdminAuthService {

    // 登录验证，仅返回生成的 Token
    String login(String username, String password);

    // 退出登录
    void logout();

    // 获取菜单列表
    List<MenuVO> getAuthMenuList();

    // 获取按钮列表
    Map<String, List<String>> getAuthButtonList();

    //获取用户信息
    UserInfoVO getUserInfo();
}
