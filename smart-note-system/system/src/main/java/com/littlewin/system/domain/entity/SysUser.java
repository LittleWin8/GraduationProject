package com.littlewin.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser implements Serializable {

    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;
    private String nickname;
    private String avatar;
    private Integer status;
    private Integer delFlag;
    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
