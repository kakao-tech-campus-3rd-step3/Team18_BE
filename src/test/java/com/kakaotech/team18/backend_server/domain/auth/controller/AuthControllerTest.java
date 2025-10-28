package com.kakaotech.team18.backend_server.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.domain.user.repository.UserRepository;
import com.kakaotech.team18.backend_server.global.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional // 각 테스트 후 DB 롤백
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtProvider jwtProvider;

    private User testUser;
    private String accessToken;
    private String refreshToken;

    // 각 테스트가 실행되기 전에 테스트용 사용자 및 토큰을 설정합니다.
    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .kakaoId(12345L)
                .name("테스트유저")
                .studentId("20241234")
                .email("test@test.com")
                .phoneNumber("010-1234-5678")
                .department("컴퓨터공학과")
                .build();
        userRepository.save(testUser);

        accessToken = jwtProvider.createAccessToken(testUser);
        refreshToken = jwtProvider.createRefreshToken(testUser);
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_success() throws Exception {
        // given
        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isOk());
    }

    @Test
    @DisplayName("로그아웃 후, 이전 Access Token으로 보호된 API 접근 시도 시 401 응답")
    void access_protected_api_after_logout_fails() throws Exception {
        // given
        // 1. 먼저 로그아웃을 수행합니다.
        mockMvc.perform(
                post("/api/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
        ).andExpect(status().isOk());

        // when
        // 2. 로그아웃에 사용했던 Access Token으로 보호된 API에 접근을 시도합니다.
        ResultActions resultActions = mockMvc.perform(
                get("/api/auth/reissue")
                        .header("Authorization", "Bearer " + accessToken)
        );

        // then
        // 3. 401 Unauthorized 응답을 기대합니다.
        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그아웃 후, 이전 Refresh Token으로 재발급 시도 시 401 응답")
    void reissue_after_logout_fails() throws Exception {
        // given
        // 1. 먼저 로그아웃을 수행합니다.
        mockMvc.perform(
                post("/api/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
        ).andExpect(status().isOk());

        // when
        // 2. 로그아웃 시 무효화된 Refresh Token으로 재발급을 시도합니다.
        ResultActions resultActions = mockMvc.perform(
                post("/api/auth/reissue")
                        .header("Authorization", "Bearer " + refreshToken)
        );

        // then
        // 3. 401 Unauthorized 응답을 기대합니다. (LoggedOutUserException 발생)
        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("이미 로그아웃된 토큰으로 다시 로그아웃 시도 시 401 응답")
    void double_logout_fails() throws Exception {
        // given
        // 1. 먼저 로그아웃을 수행합니다.
        mockMvc.perform(
                post("/api/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
        ).andExpect(status().isOk());

        // when
        // 2. 방금 블랙리스트에 등록된 Access Token으로 다시 로그아웃을 시도합니다.
        ResultActions resultActions = mockMvc.perform(
                post("/api/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
        );

        // then
        // 3. 401 Unauthorized 응답을 기대합니다. (BLACKLISTED_TOKEN 에러 발생)
        resultActions.andExpect(status().isUnauthorized());
    }
}