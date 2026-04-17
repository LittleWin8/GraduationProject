package com.littlewin.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_role")
public class SysRole implements Serializable {

    @TableId(value = "role_id", type = IdType.AUTO)
    private Long roleId;
    private String roleName;
    private String roleKey;
    private Integer sortOrder;
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}