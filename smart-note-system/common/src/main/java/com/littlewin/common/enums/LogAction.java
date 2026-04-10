package com.littlewin.common.enums;

import lombok.Getter;

@Getter
public enum LogAction {
    LOGIN(1, "登录"),
    LOGOUT(2, "退出"),
    CREATE(3, "创建"),
    UPDATE(4, "修改"),
    DELETE(5, "删除");

    private final Integer code;
    private final String info;

    LogAction(Integer code, String info) {
        this.code = code;
        this.info = info;
    }
}