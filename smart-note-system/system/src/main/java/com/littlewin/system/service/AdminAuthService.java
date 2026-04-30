package com.littlewin.system.service;

import com.littlewin.system.domain.dto.SecurityUpdateDTO;
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

    // 获取用户信息
    UserInfoVO getUserInfo();

    // 修改个人手机号和密码
    void updateSecurityInfo(SecurityUpdateDTO dto);

    // 将当前 token 加入 Redis 黑名单（退出登录时调用）
    void addTokenToBlacklist(String token);
}
