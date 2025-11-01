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
  
  
    @MockBean
    private AuthService authService;

    @MockBean
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
                post("/api/auth/reissue")
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

    @DisplayName("카카오 로그인 성공 - 기존 회원")
    @Test
    void kakaoLogin_existingUser_success() throws Exception {
        // given
        String authorizationCode = "testAuthCode";
        String accessToken = "mockAccessToken";
        String refreshToken = "mockRefreshToken";
        KakaoLoginRequestDto requestDto = new KakaoLoginRequestDto(authorizationCode);

        LoginSuccessResponseDto serviceResponse = new LoginSuccessResponseDto(AuthStatus.LOGIN_SUCCESS, accessToken, refreshToken, List.of());

        given(authService.kakaoLogin(authorizationCode)).willReturn(serviceResponse);

        // when & then
        mockMvc.perform(post("/api/auth/kakao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().secure("refreshToken", true))
                .andExpect(cookie().path("refreshToken", "/"))
                .andExpect(jsonPath("$.status").value(AuthStatus.LOGIN_SUCCESS.name()))
                .andExpect(jsonPath("$.accessToken").value(accessToken))
                .andExpect(jsonPath("$.refreshToken").doesNotExist()); // 응답 본문에 refreshToken이 없어야 함
    }

    @DisplayName("카카오 로그인 성공 - 신규 회원 (추가 정보 필요)")
    @Test
    void kakaoLogin_newUser_registrationRequired() throws Exception {
        // given
        String authorizationCode = "testAuthCode";
        String temporaryToken = "mockTemporaryToken";
        KakaoLoginRequestDto requestDto = new KakaoLoginRequestDto(authorizationCode);
        RegistrationRequiredResponseDto serviceResponse = new RegistrationRequiredResponseDto(AuthStatus.REGISTRATION_REQUIRED, temporaryToken);

        given(authService.kakaoLogin(authorizationCode)).willReturn(serviceResponse);

        // when & then
        mockMvc.perform(post("/api/auth/kakao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist("refreshToken")) // 신규 회원은 refreshToken 쿠키가 없어야 함
                .andExpect(jsonPath("$.status").value(AuthStatus.REGISTRATION_REQUIRED.name()))
                .andExpect(jsonPath("$.temporaryToken").value(temporaryToken));
    }

    @DisplayName("회원가입 성공")
    @Test
    void register_success() throws Exception {
        // given
        String temporaryToken = "mockTemporaryToken";
        String accessToken = "newAccessToken";
        String refreshToken = "newRefreshToken";
        RegisterRequestDto requestDto = new RegisterRequestDto(
                "testUser", "test@example.com", "123456", "컴퓨터공학과", "010-1234-5678"
        );

        LoginSuccessResponseDto serviceResponse = new LoginSuccessResponseDto(AuthStatus.REGISTER_SUCCESS, accessToken, refreshToken, List.of());

        given(authService.register(anyString(), any(RegisterRequestDto.class))).willReturn(serviceResponse);

        // when & then
        mockMvc.perform(post("/api/auth/register")
                        .header("Authorization", "Bearer " + temporaryToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().secure("refreshToken", true))
                .andExpect(cookie().path("refreshToken", "/"))
                .andExpect(jsonPath("$.status").value(AuthStatus.REGISTER_SUCCESS.name()))
                .andExpect(jsonPath("$.accessToken").value(accessToken))
                .andExpect(jsonPath("$.refreshToken").doesNotExist());
    }

    @DisplayName("Access Token 재발급 성공")
    @Test
    void reissue_success() throws Exception {
        // given
        String oldRefreshToken = "oldMockRefreshToken";
        String newAccessToken = "newMockAccessToken";
        String newRefreshToken = "newMockRefreshToken";
        ReissueResponseDto serviceResponse = ReissueResponseDto.of(newAccessToken, newRefreshToken);

        given(authService.reissue(oldRefreshToken)).willReturn(serviceResponse);

        // when & then
        mockMvc.perform(post("/api/auth/reissue")
                        .cookie(new Cookie("refreshToken", oldRefreshToken))) // 쿠키에 refreshToken 담아 전송
                .andExpect(status().isOk())
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().secure("refreshToken", true))
                .andExpect(cookie().path("refreshToken", "/"))
                .andExpect(jsonPath("$.accessToken").value(newAccessToken))
                .andExpect(jsonPath("$.refreshToken").doesNotExist()); // 응답 본문에 refreshToken이 없어야 함
    }

    @DisplayName("Access Token 재발급 실패 - Refresh Token 쿠키 없음")
    @Test
    void reissue_fail_noRefreshTokenCookie() throws Exception {
        // given (authService.reissue는 호출되지 않을 것이므로 given 설정 불필요)

        // when & then
        mockMvc.perform(post("/api/auth/reissue")) // 쿠키 없이 요청
                .andExpect(status().isBadRequest()); // @CookieValue는 쿠키가 없으면 400 Bad Request 반환
    }
}
