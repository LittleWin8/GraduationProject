package com.littlewin.system.service;

import com.littlewin.system.domain.vo.UserInfoVO;

import java.util.Map;

public interface WxUserService {
    /**
     * 微信用户登录
     * @return 包含 token 和 isNewUser 的键值对
     */
    Map<String, Object> login(String code, String nickName, String avatarUrl);

    /**
     * 微信用户获取个人详细信息
     */
    UserInfoVO getUserInfo();
}
