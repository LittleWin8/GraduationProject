package com.littlewin.system.service.impl;

import com.littlewin.system.domain.dto.AdminLoginDTO;
import com.littlewin.system.mapper.UserAuthMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring Security 的用户详情服务实现
 * 这个类的作用是让 Spring Security 知道如何从数据库加载用户信息
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserAuthMapper userAuthMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 从数据库查询用户信息
        AdminLoginDTO user = userAuthMapper.selectAdminLoginUser(username);

        if (user == null) {
            throw new UsernameNotFoundException("登录用户：" + username + " 不存在");
        }

        if (user.getStatus() == 0) {
            throw new UsernameNotFoundException("登录用户：" + username + " 已禁用");
        }

        // 2. 加载权限标志位
        List<String> perms = userAuthMapper.selectMenuPermsByUserId(user.getUserId());

        // 3.转换成 Spring Security 识别的权限对象
        List<SimpleGrantedAuthority> authorities = perms.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // 4. 构建 Spring Security 的 UserDetails 对象
        return User.withUsername(user.getUsername())
                .password(user.getPassword()) // 这里的密码必须是数据库里存的加密后的字符串
                .authorities(authorities)
                .build();
    }
}