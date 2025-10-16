package com.kakaotech.team18.backend_server.global.security;

import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.domain.user.repository.UserRepository;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * @deprecated 이 메서드는 UserDetailsService 인터페이스와의 계약을 위해 존재합니다.
     *             JWT 인증 시에는 토큰의 memberships 정보가 필요하므로,
     *             대신 {@link #loadUserByUsername(String, Map)}를 사용해야 합니다.
     * @since 1.0.0
     * @forRemoval true
     */
    @Override
    @Deprecated(since = "1.0.0", forRemoval = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // memberships 정보 없이 호출될 경우, 빈 맵을 전달하여 핵심 로직을 수행하는 메서드를 호출합니다.
        return this.loadUserByUsername(username, Collections.emptyMap());
    }

    /**
     * JWT 필터에서 호출할 커스텀 메서드입니다.
     * 토큰에서 추출한 subject(userId)와 memberships 정보를 모두 받아 완전한 PrincipalDetails 객체를 생성합니다.
     *
     * @param username 토큰의 subject, 즉 사용자의 ID(String 형태)
     * @param memberships 토큰의 "memberships" 클레임에서 추출한 역할 정보 맵
     * @return UserDetails를 구현한 PrincipalDetails 객체
     * @throws UsernameNotFoundException 해당 ID의 사용자를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username, Map<String, String> memberships) throws UsernameNotFoundException {
        final Long userId;
        try {
            userId = Long.parseLong(username);
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("유효하지 않은 사용자 식별자(subject): " + username, e);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("해당 ID의 사용자를 찾을 수 없습니다: " + userId));

        // User 정보와 함께 memberships 정보도 PrincipalDetails 생성자에 전달합니다.
        return new PrincipalDetails(user, memberships);
    }
}
