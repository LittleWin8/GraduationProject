package com.littlewin.system.domain.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 用户查询请求参数对象
 */
@Data
public class UserQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 当前页码 */
    private Integer pageNum = 1;
    /** 每页条数 */
    private Integer pageSize = 10;

    /** 认证类型 (password / wx_openid) */
    private String authType;
    /** 用户名/标识 (支持模糊查询，仅限服务端用户) */
    private String identifier;
    /** 性别 (0未知, 1男, 2女) */
    private Integer gender;
    /** 城市 */
    private String city;
    /** 手机号 */
    private String phone;
    /** 角色名 (用于按角色检索用户) */
    private List<Long> roleId;
    /** 状态 (1 正常，0 禁用，2 注销) */
    private Integer status;
    /** 注册开始时间 (yyyy-MM-dd HH:mm:ss) */
    private String startTime;
    /** 注册结束时间 (yyyy-MM-dd HH:mm:ss) */
    private String endTime;
}