package com.littlewin.common.core;

import lombok.Data;

@Data
public class LoginDTO {

    private Long userId;
    private Integer status;
    private String username;
    private String password;


}