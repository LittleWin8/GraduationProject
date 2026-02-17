package com.littlewin.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("user_auth")
public class UserAuth implements Serializable {

    @TableId(value = "auth_id", type = IdType.AUTO)
    private Long authId;

    private Long userId;

    private String authType;

    private String identifier;

    private String credential;

    private LocalDateTime createTime;
}
