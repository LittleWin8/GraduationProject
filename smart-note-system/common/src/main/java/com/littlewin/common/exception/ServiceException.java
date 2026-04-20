package com.littlewin.common.exception;

public class ServiceException extends RuntimeException {

    private final int code;

    public ServiceException(String message) {
        super(message);
        this.code = 400;
    }

    public ServiceException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}