package com.littlewin.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 手机号/密码修改通用 DTO
 */
@Data
public class SecurityUpdateDTO {
    @NotNull(message = "修改类型不能为空")
    private Integer type; // 1: 修改密码, 2: 修改手机号

    @NotBlank(message = "原凭证不能为空")
    private String oldField; // 对应：旧密码 或 验证码

    @NotBlank(message = "新凭证不能为空")
    private String newField; // 对应：新密码 或 新手机号
}