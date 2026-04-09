package com.littlewin.system.service;

public interface AdminAuthService {

    String login(String userId, String password);
    void logout();
}
