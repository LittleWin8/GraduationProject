package com.littlewin.system.service.impl;

import com.littlewin.common.exception.ServiceException;
import com.littlewin.common.utils.JwtUtils;
import com.littlewin.system.service.SysLoginService;

import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.stereotype.Service;

@Service
public class SysLoginServiceImpl implements SysLoginService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public String login(String userId, String password) {

        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(userId, password);

        Authentication authentication =
                authenticationManager.authenticate(token);

        if (authentication == null) {
            throw new ServiceException("登录失败");
        }

        return JwtUtils.createToken(userId);
    }
}
