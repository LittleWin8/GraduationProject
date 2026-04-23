package com.littlewin.common.core;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;

/**
 * 安全上下文容器：包装 DTO，实现 UserDetails 接口
 */
public record LoginUser(LoginDTO adminLoginDTO,
                        Collection<? extends GrantedAuthority> authorities) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return adminLoginDTO.getPassword();
    }

    @Override
    public String getUsername() {
        return adminLoginDTO.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() {
        //  1 为正常
        return adminLoginDTO.getStatus() != null && adminLoginDTO.getStatus() == 1;
    }
}