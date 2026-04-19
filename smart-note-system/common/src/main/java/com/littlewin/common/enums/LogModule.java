package com.littlewin.common.enums;

import lombok.Getter;

/**
 * 日志模块
 */
@Getter
public enum LogModule {

    AUTH("AUTH"),
    USER("USER"),
    NOTE("NOTE"),
    DICT("DICT"),
    AI("AI"),
    ROLE("ROLE");
    private final String module;

    LogModule(String module) {
        this.module = module;
    }
}