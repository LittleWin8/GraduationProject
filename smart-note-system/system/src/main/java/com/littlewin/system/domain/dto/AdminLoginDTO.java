package com.littlewin.system.domain.dto;

import lombok.Data;

@Data
public class AdminLoginDTO {

    private Long userId;
    private Integer status;
    private String username;
    private String password;


}