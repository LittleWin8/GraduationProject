package com.littlewin.system.domain.dto;

import lombok.Data;

@Data
public class WxUserUpdateDTO {

    private String name;

    private String nickname;

    private String avatar;

    private Integer gender;

    private String phone;

    private String email;

    private String birthday;

    private String city;

    private String signature;
}
