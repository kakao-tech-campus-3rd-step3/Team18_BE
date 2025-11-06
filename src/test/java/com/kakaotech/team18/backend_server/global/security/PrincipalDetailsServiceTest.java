package com.kakaotech.team18.backend_server.global.security;

import com.kakaotech.team18.backend_server.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PrincipalDetailsServiceTest {

    @InjectMocks
    private PrincipalDetailsService principalDetailsService;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("사용자 인증 정보 조회 - 성공")
    void loadUserByUsername_success() {
        // given
        final Long userId = 1L;
        final String username = userId.toString();

        // userRepository.existsById가 true를 반환하도록 설정
        given(userRepository.existsById(userId)).willReturn(true);

        // when
        UserDetails userDetails = principalDetailsService.loadUserByUsername(username, Collections.emptyMap());

        // then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName("사용자 인증 정보 조회 - 실패 (존재하지 않는 사용자)")
    void loadUserByUsername_fail_userNotFound() {
        // given
        final Long nonExistentUserId = 9999L;
        final String username = nonExistentUserId.toString();

        // userRepository.existsById가 false를 반환하도록 설정
        given(userRepository.existsById(nonExistentUserId)).willReturn(false);

        // when & then
        assertThrows(UsernameNotFoundException.class, () -> {
            principalDetailsService.loadUserByUsername(username, Collections.emptyMap());
        });
    }

    @Test
    @DisplayName("사용자 인증 정보 조회 - 실패 (잘못된 형식의 사용자 ID)")
    void loadUserByUsername_fail_invalidUsernameFormat() {
        // given
        final String invalidUsername = "invalid-user-id";

        // when & then
        assertThrows(UsernameNotFoundException.class, () -> {
            principalDetailsService.loadUserByUsername(invalidUsername, Collections.emptyMap());
        });
    }
}
