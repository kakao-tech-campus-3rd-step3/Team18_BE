package com.kakaotech.team18.backend_server.global.security;

import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.domain.user.repository.UserRepository;
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
     * Spring Security가 사용자를 인증할 때 호출하는 메서드입니다.
     * 우리 시스템에서는 JWT 필터에서 토큰 검증 후, 토큰의 subject(userId)를 이 메서드의 username 파라미터로 전달하여 호출합니다.
     *
     * @param username 토큰의 subject, 즉 사용자의 ID(String 형태)
     * @return UserDetails를 구현한 PrincipalDetails 객체
     * @throws UsernameNotFoundException 해당 ID의 사용자를 찾을 수 없는 경우
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final Long userId;
        try {
            userId = Long.parseLong(username);
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("유효하지 않은 사용자 식별자(subject): " + username, e);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("해당 ID의 사용자를 찾을 수 없습니다: " + userId));

        return new PrincipalDetails(user);
    }
}
