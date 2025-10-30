package com.kakaotech.team18.backend_server.global.security;

import com.kakaotech.team18.backend_server.domain.user.repository.UserRepository;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * @deprecated 이 메서드는 UserDetailsService 인터페이스와의 계약을 위해 존재합니다.
     *             JWT 인증 시에는 토큰의 memberships 정보가 필요하므로,
     *             대신 {@link #loadUserByUsername(String, Map)}를 사용해야 합니다.
     */
    @Override
    @Deprecated(since = "1.0.0", forRemoval = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Long userId = Long.parseLong(username);
        // DB에서 User 엔티티를 조회하는 대신, 존재 여부만 확인합니다.
        if (!userRepository.existsById(userId)) {
            throw new UsernameNotFoundException("해당 ID의 사용자를 찾을 수 없습니다: " + username);
        }
        // 존재한다면, DB 조회 없이 PrincipalDetails 객체를 생성합니다.
        return new PrincipalDetails(userId, Collections.emptyMap());
    }

    /**
     * JWT 필터에서 호출할 커스텀 메서드입니다.
     * 이 메서드는 DB 조회를 수행하지 않습니다.
     */
    public UserDetails loadUserByUsername(String username, Map<String, String> memberships) throws UsernameNotFoundException {
        final Long userId;
        try {
            userId = Long.parseLong(username);
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("유효하지 않은 사용자 식별자(subject): " + username, e);
        }

        // DB 조회 없이, userId와 memberships만으로 PrincipalDetails를 생성합니다.
        return new PrincipalDetails(userId, memberships);
    }
}
