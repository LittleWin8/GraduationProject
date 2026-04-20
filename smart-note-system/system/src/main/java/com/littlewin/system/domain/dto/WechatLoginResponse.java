package com.littlewin.system.domain.dto;

import lombok.Data;

@Data
public class WechatLoginResponse {
    private String openid;      // 用户唯一标识
    private String session_key;  // 会话密钥
    private String unionid;      // 用户在开放平台的唯一标识（需绑定）
    private Integer errcode;     // 错误码
    private String errmsg;      // 错误信息
}