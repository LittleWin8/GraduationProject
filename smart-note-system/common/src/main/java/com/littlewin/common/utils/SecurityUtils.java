package com.littlewin.common.utils;

import com.littlewin.common.core.LoginDTO;
import com.littlewin.common.core.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    /**
     * 获取 Authentication
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取当前登录用户
     */
    public static LoginDTO getLoginUser() {
        Authentication authentication = getAuthentication();

        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof LoginUser loginUser) {
            return loginUser.adminLoginDTO();
        }

        return null;
    }

    public static String getUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
