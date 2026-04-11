package com.littlewin.system.domain.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserListVO {
    private Long userId;
    private String nickname;
    private String avatar;
    private String account;    // 对应 user_auth 里的 identifier
    private String authType;
    private String phone;
    private Integer status;
    private String roleNames;  // 角色名称拼接，如 "管理员,测试"
    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;
}