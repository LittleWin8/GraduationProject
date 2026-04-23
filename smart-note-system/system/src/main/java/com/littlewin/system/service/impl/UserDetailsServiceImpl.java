package com.littlewin.system.service.impl;

import com.littlewin.common.core.LoginDTO;
import com.littlewin.common.core.LoginUser;
import com.littlewin.system.mapper.UserAuthMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
        LoginDTO user = userAuthMapper.selectAdminLoginUser(username);

        if (user == null) {
            throw new UsernameNotFoundException("登录用户：" + username + " 不存在");
        }

        if (user.getStatus() == 0) {
            // 抛出禁用异常
            throw new DisabledException("对不起，您的账号已被禁用");
        }

        if (user.getStatus() == 2) {
            // 抛出账户过期/注销异常
            throw new AccountExpiredException("对不起，您的账号已注销");
        }

        // 2. 加载权限标志位
        List<String> perms = userAuthMapper.selectMenuPermsByUserId(user.getUserId());

        // 3.转换成 Spring Security 识别的权限对象
        List<SimpleGrantedAuthority> authorities = perms.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // 4. 构建 Spring Security 的 UserDetails 对象
        return new LoginUser(user, authorities);
    }
}