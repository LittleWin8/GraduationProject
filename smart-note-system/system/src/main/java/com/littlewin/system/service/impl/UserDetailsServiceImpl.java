package com.littlewin.system.service.impl;

import com.littlewin.system.domain.dto.AdminLoginDTO;
import com.littlewin.system.mapper.UserAuthMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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

        // 2. 构建 Spring Security 的 UserDetails 对象
        // 注意：这里只传入了用户名和密码，权限信息（Authorities）暂时为空，由后续过滤器处理
        return User.withUsername(user.getUsername())
                .password(user.getPassword()) // 这里的密码必须是数据库里存的加密后的字符串
                .build();
    }
}