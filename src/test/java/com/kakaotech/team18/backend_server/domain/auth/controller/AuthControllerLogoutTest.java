package com.kakaotech.team18.backend_server.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.domain.user.repository.UserRepository;
import com.kakaotech.team18.backend_server.global.security.JwtProperties;
import com.kakaotech.team18.backend_server.global.security.JwtProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
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
class AuthControllerLogoutTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private JwtProperties jwtProperties;

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
                get("/api/clubs/1/dashboard")
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
                        .cookie(new MockCookie("refreshToken", refreshToken))
        );

        // then
        // 3. 401 Unauthorized 응답을 기대합니다. (LoggedOutUserException 발생)
        resultActions.andExpect(status().isUnauthorized());
    }

    @DisplayName("만료된 토큰으로 재발급 요청 시, 필터를 통과하여 컨트롤러에 도달한다")
    @Test
    void doFilterInternal_should_pass_when_token_is_expired_for_reissue() throws Exception {
        // given
        // 일부러 만료된 토큰을 생성. 필터가 만료 예외를 잡고 통과시키는지 확인하기 위함.
        Date now = new Date();
        Date expiredValidity = new Date(now.getTime() - 10000); // 10초 전에 만료된 시간
        Key key = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));

        String expiredToken = Jwts.builder()
                .setSubject(testUser.getId().toString())
                .claim("tokenType", "ACCESS")
                .setIssuedAt(now)
                .setExpiration(expiredValidity)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        // when
        // 만료된 토큰을 헤더에 담아 /api/auth/reissue 로 요청
        ResultActions resultActions = mockMvc.perform(
                post("/api/auth/reissue")
                        .header("Authorization", "Bearer " + expiredToken)
        );

        // then
        // JwtAuthenticationFilter가 만료된 토큰을 통과시켰다면, 요청은 컨트롤러까지 도달한다.
        // 컨트롤러에서는 @CookieValue에 refreshToken이 없으므로 400 Bad Request를 반환한다.
        // 만약 필터가 요청을 막았다면 401 Unauthorized가 반환될 것이다.
        resultActions.andExpect(status().isBadRequest());
    }
}