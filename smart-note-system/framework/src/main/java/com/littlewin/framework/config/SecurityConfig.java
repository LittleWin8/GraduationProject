package com.littlewin.framework.config;

import com.littlewin.framework.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.annotation.Resource;

@Configuration
public class SecurityConfig {

    @Resource
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 1. 注入 AuthenticationConfiguration
     * 这是 Spring Security 提供的配置类，用于获取认证管理器
     */
    private final AuthenticationConfiguration authenticationConfiguration;

    // 通过构造函数注入
    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration) {
        this.authenticationConfiguration = authenticationConfiguration;
    }

    /**
     * 2. 暴露 AuthenticationManager Bean
     * 这一步是为了解决 "找不到 'AuthenticationManager' 类型的 Bean" 的问题
     * 这样 AdminAuthServiceImpl 才能注入并使用它
     */
    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * 3. 配置密码编码器
     * 告诉 Spring Security 使用 BCrypt 算法比对数据库中的密码
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 4. 原有的安全过滤链配置
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .authorizeHttpRequests(auth -> auth
                        // 登录和退出都设置为 permitAll()
                        .requestMatchers("/api/admin/auth/login","/api/admin/auth/logout", "/api/wx/auth/login").permitAll()
                        // 其他所有请求需要认证
                        .anyRequest().authenticated()
                );

        // 👇 加入 JWT 过滤器（放在用户名密码过滤器之前）
        http.addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }
}