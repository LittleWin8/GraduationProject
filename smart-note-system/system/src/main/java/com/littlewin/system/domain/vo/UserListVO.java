package com.littlewin.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户列表返回对象
 */
@Data
public class UserListVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String nickname;
    private String authType;
    private String identifier;
    private Integer gender;
    private String phone;
    private String city;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}