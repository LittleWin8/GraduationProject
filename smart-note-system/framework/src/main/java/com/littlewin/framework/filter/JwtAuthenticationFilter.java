package com.littlewin.framework.filter;

import com.littlewin.common.constants.Constants;
import com.littlewin.common.constants.RedisKeyConstants;
import com.littlewin.common.redis.RedisService;
import com.littlewin.common.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 * 1. 从请求头提取 Token
 * 2. 校验 Token 是否在黑名单（已登出）
 * 3. 未登出则解析 Token 并写入 Spring Security 上下文
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Resource
    private UserDetailsService userDetailsService;

    @Resource
    private RedisService redisService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader(Constants.TOKEN_HEADER);

        if (StringUtils.hasText(header) && header.startsWith(Constants.TOKEN_PREFIX)) {

            String token = header.substring(Constants.TOKEN_PREFIX.length());

            // 检查 Redis 黑名单：已登出的 token（按 jti）直接拒绝
            if (redisService.hasKey(RedisKeyConstants.TOKEN_BLACKLIST + JwtUtils.getTokenId(token))) {
                SecurityContextHolder.clearContext();
            } else {
                try {
                    String userId = JwtUtils.getSubject(token);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(userId);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                } catch (Exception e) {
                    SecurityContextHolder.clearContext();
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}