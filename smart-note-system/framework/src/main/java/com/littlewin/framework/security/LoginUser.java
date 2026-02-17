package com.littlewin.framework.security;

import com.littlewin.system.domain.entity.SysUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class LoginUser implements UserDetails {

    private SysUser user;

    /** 登录凭证（来自 user_auth） */
    private String password;

    /** 登录标识（username / email / openid） */
    private String username;

    public LoginUser(SysUser user, String username, String password) {
        this.user = user;
        this.username = username;
        this.password = password;
    }

    public SysUser getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() {
        return user.getStatus() == 1;
    }
}
