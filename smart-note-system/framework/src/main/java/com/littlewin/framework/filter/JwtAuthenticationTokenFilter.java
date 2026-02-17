package com.littlewin.framework.filter;

import com.littlewin.common.constants.Constants;
import com.littlewin.common.utils.JwtUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    jakarta.servlet.http.HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader(Constants.TOKEN_HEADER);

        if (header != null && header.startsWith(Constants.TOKEN_PREFIX)) {
            String token = header.replace(Constants.TOKEN_PREFIX, "");
            String username = JwtUtils.getSubject(token);

            // 简化版：这里只解析，不做权限加载
            if (username != null) {
                SecurityContextHolder.getContext().setAuthentication(null);
            }
        }

        filterChain.doFilter(request, response);
    }
}
