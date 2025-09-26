package com.kakaotech.team18.backend_server.global.security;

import com.kakaotech.team18.backend_server.domain.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class PrincipalDetails implements UserDetails {

    private final User user;

    public PrincipalDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // TODO: 사용자의 실제 역할(Role) 정보를 반환하도록 수정 필요
        // 지금은 빈 권한 목록을 반환합니다.
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        // 우리는 JWT를 사용하므로, 비밀번호는 사용하지 않습니다.
        return null;
    }

    @Override
    public String getUsername() {
        // Spring Security에서 'username'은 사용자를 식별하는 고유 ID를 의미합니다.
        // 우리 시스템에서는 User의 ID (Long)를 문자열로 변환하여 사용합니다.
        return user.getId().toString();
    }

    // 계정이 만료되지 않았는지 리턴 (true: 만료안됨)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정이 잠겨있지 않은지 리턴 (true: 잠기지 않음)
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 비밀번호가 만료되지 않았는지 리턴 (true: 만료안됨)
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정이 활성화(사용가능)인지 리턴 (true: 활성화)
    @Override
    public boolean isEnabled() {
        return true;
    }
}
