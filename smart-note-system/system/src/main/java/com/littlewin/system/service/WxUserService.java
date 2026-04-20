package com.littlewin.system.service;

public interface WxUserService {
    /**
     * 小程序登录接口
     * @param code 微信临时登录凭证
     * @return 登录成功后的 JWT Token
     */
    String login(String code);
}
