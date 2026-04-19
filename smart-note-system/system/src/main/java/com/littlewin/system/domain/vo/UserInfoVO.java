package com.littlewin.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 当前登录用户信息视图对象
 */
@Data
public class UserInfoVO {


    // 1. 系统用户基础信息 (来自 sys_user 表)
    /** 用户ID */
    private Long userId;

    /** 用户昵称 */
    private String name;

    /** 头像地址 */
    private String avatar;

    /** 账号创建时间 (注册日期) */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;


    // 2. 账号认证信息 (来自 user_auth 表)
    /** 登录账号 (用户名) */
    private String account;

    // 3. 用户详细资料 (来自 user_info 表)
    /** 性别：0 未知, 1 男, 2 女 */
    private Integer gender;

    /** 手机号码 */
    private String phone;

    /** 电子邮箱 */
    private String email;

    /** 生日 (格式: yyyy-MM-dd) */
    private String birthday;

    /** 所在城市 */
    private String city;

    /** 个性签名 */
    private String signature;


    /** 角色权限标识列表 (如: ['1', '2']) */
    private List<String> roles;
}