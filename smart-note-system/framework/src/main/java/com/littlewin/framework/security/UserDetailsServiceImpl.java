package com.littlewin.framework.security;

import com.littlewin.system.domain.entity.UserAuth;
import com.littlewin.system.domain.entity.SysUser;
import com.littlewin.system.mapper.SysUserMapper;
import com.littlewin.system.mapper.UserAuthMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserAuthMapper userAuthMapper;

    @Autowired
    private SysUserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        // 1. 查询认证信息
        UserAuth auth = userAuthMapper.selectByIdentifier(username);

        if (auth == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        // 2. 查询用户信息
        SysUser user = userMapper.selectById(auth.getUserId());

        if (user == null || user.getStatus() == 0) {
            throw new UsernameNotFoundException("用户被禁用");
        }

        // 3. 返回 Security 用户对象
        return new LoginUser(
                user,
                auth.getIdentifier(),
                auth.getCredential()
        );
    }
}
