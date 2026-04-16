package com.littlewin.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UserDetailsVO {
    // 1. 基础信息 (sys_user)
    private Long userId;
    private String nickname;
    private String avatar;
    private Integer status;

    // 2. 认证信息 (user_auth)
    private String identifier;
    private String authType;

    // 3. 详细资料 (user_info)
    private Integer gender;
    private String phone;
    private String email;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate birthday;
    private String city;
    private String signature;

    // 4. 角色信息 (核心补充)
    private List<Long> roleIds;       // 用于编辑表单回显 (如: [1, 5])
    private List<String> roleNames;   // 用于详情页展示 (如: ["管理员", "开发人员"])
}