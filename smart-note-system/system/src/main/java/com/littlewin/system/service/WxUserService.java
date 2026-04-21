package com.littlewin.system.service;

public interface WxUserService {
    /* 微信用户登录 */
    String login(String code, String nickName, String avatarUrl);
}
