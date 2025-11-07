package com.kakaotech.team18.backend_server.global.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@RequiredArgsConstructor
public class PrincipalDetails implements UserDetails {

    private final Long userId;
    private final Map<String, String> memberships;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (memberships == null || memberships.isEmpty()) {
            return Collections.emptyList();
        }

        return memberships.entrySet().stream()
                .map(entry -> new SimpleGrantedAuthority("CLUB_" + entry.getKey() + "_" + entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        // 우리는 JWT를 사용하므로, 비밀번호는 사용하지 않습니다.
        return null;
    }

    @Override
    public String getUsername() {
        return this.userId.toString();
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
