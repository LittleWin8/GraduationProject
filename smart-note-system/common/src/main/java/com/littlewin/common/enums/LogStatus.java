package com.littlewin.common.enums;

import lombok.Getter;

@Getter
public enum LogStatus {
    SUCCESS(1), FAIL(0);
    private final Integer code;
    LogStatus(Integer code) { this.code = code; }
}