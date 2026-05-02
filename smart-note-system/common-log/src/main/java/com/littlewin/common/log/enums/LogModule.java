package com.littlewin.common.log.enums;

import lombok.Getter;

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
