package com.kakaotech.team18.backend_server.global.security;

import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.domain.user.repository.UserRepository;
import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class JwtAuthenticationFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtProperties jwtProperties;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        testUser = User.builder()
                .name("testUser")
                .email("test@test.com")
                .phoneNumber("010-1234-5678")
                .studentId("12345678")
                .department("컴퓨터공학과")
                .build();
        userRepository.save(testUser);
    }

    @DisplayName("만료된 토큰으로 재발급 요청 시, 필터를 통과하여 컨트롤러에 도달한다")
    @Test
    void doFilterInternal_should_pass_when_token_is_expired_for_reissue() throws Exception {
        // given
        // 일부러 만료된 토큰을 생성. 필터가 만료 예외를 잡지 않고 통과시키는지 확인하기 위함.
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

    @DisplayName("잘못된 서명의 토큰으로 요청 시 401 응답을 반환한다")
    @Test
    void doFilterInternal_with_invalid_signature_token_returns_401() throws Exception {
        // given
        String invalidSignatureToken = createTokenWithInvalidSignature(testUser);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/clubs")
                        .header("Authorization", "Bearer " + invalidSignatureToken)
        );

        // then
        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_JWT_SIGNATURE.getMessage()))
                .andDo(print());
    }

    @DisplayName("잘못된 형식의 토큰으로 요청 시 401 응답을 반환한다")
    @Test
    void doFilterInternal_with_malformed_token_returns_401() throws Exception {
        // given
        String malformedToken = "this.is.not.a.valid.jwt";

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/clubs")
                        .header("Authorization", "Bearer " + malformedToken)
        );

        // then
        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(ErrorCode.MALFORMED_JWT.getMessage()))
                .andDo(print());
    }

    @DisplayName("만료된 토큰으로 일반 API 요청 시 401 응답을 반환한다")
    @Test
    void doFilterInternal_with_expired_token_returns_401() throws Exception {
        // given
        // 일부러 만료된 토큰을 생성
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
        // 만료된 토큰을 헤더에 담아 일반 API(/api/clubs)로 요청
        ResultActions resultActions = mockMvc.perform(
                get("/api/clubs")
                        .header("Authorization", "Bearer " + expiredToken)
        );

        // then
        // JwtAuthenticationFilter가 ExpiredJwtException을 잡아서 401을 반환해야 한다.
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(ErrorCode.EXPIRED_ACCESS_TOKEN.getMessage()));
    }

    private String createTokenWithInvalidSignature(User user) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtProperties.accessTokenValidityInSeconds() * 1000);
        // HS512 알고리즘은 512비트(64바이트) 이상의 키를 요구합니다.
        String invalidSecret = "this-is-a-completely-different-and-long-enough-secret-key-for-testing-purposes";
        Key invalidKey = Keys.hmacShaKeyFor(invalidSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("tokenType", "ACCESS")
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(invalidKey, SignatureAlgorithm.HS512)
                .compact();
    }
}
