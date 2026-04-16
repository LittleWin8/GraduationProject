package com.littlewin.system.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 用户修改传输对象
 */
@Data
public class UserUpdateDTO {

    private Long userId;
    // sys_user 字段
    private String nickname;
    private String avatar;
    private Integer status;

    // user_info 字段
    private Integer gender;
    private String phone;
    private String email;
    private String city;
    private String signature;
    // 角色关联
    private List<Long> roleIds;
}
