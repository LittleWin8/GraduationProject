package com.littlewin.system.domain.dto;

import lombok.Data;

@Data
public class UserQueryDTO {
    private String nickname;
    private String phone;
    private Integer status;
    private String authType;   // 认证类型：password 或 wx_openid
    private String identifier; // 用户名或 OpenID
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
