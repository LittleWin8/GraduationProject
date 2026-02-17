package com.littlewin.framework.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.littlewin.common.core.AjaxResult;
import jakarta.servlet.http.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) {

        response.setContentType("application/json;charset=UTF-8");
        try {
            new ObjectMapper().writeValue(
                    response.getWriter(),
                    AjaxResult.error("not login"));
        } catch (Exception ignored) {}
    }
}
