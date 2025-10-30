package com.kakaotech.team18.backend_server.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kakaotech.team18.backend_server.domain.auth.dto.AuthStatus;
import com.kakaotech.team18.backend_server.domain.auth.dto.KakaoLoginRequestDto;
import com.kakaotech.team18.backend_server.domain.auth.dto.LoginSuccessResponseDto;
import com.kakaotech.team18.backend_server.domain.auth.dto.RegisterRequestDto;
import com.kakaotech.team18.backend_server.domain.auth.dto.RegistrationRequiredResponseDto;
import com.kakaotech.team18.backend_server.domain.auth.dto.ReissueResponseDto;
import com.kakaotech.team18.backend_server.domain.auth.service.AuthService;
import com.kakaotech.team18.backend_server.global.config.SecurityConfig;
import com.kakaotech.team18.backend_server.global.config.TestSecurityConfig;
import com.kakaotech.team18.backend_server.global.security.JwtAuthenticationFilter;
import com.kakaotech.team18.backend_server.global.security.JwtProperties;
import com.kakaotech.team18.backend_server.global.security.JwtProvider;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie; // jakarta.servlet.http.Cookie 임포트

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
        }
)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtProvider jwtProvider; // AuthController에 직접 주입되지는 않지만, JwtProvider가 필요한 경우를 대비하여 MockBean으로 등록

    @MockBean
    private JwtProperties jwtProperties;

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
