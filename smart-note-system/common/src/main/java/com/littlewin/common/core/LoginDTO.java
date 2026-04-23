package com.littlewin.common.core;

import lombok.Data;

@Data
public class LoginDTO {

    private Long userId;
    private Integer status;
    private String username;   // 对应 user_auth 的 identifier。web端就是登录账号，mp端就是用户的open_id
    private String password;    // 对应 user_auth 的 credential。web端就是登录密码，mp端为null


}