package com.kakaotech.team18.backend_server.domain.auth.service;

import com.kakaotech.team18.backend_server.domain.auth.dto.*;
import com.kakaotech.team18.backend_server.domain.auth.entity.RefreshToken;
import com.kakaotech.team18.backend_server.domain.auth.repository.RefreshTokenRepository;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.domain.user.repository.UserRepository;
import com.kakaotech.team18.backend_server.global.exception.exceptions.DuplicateKakaoIdException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.KakaoApiTimeoutException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.InvalidRefreshTokenException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.LoggedOutUserException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.NotRefreshTokenException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.UnauthenticatedUserException;
import com.kakaotech.team18.backend_server.global.security.JwtProperties;
import com.kakaotech.team18.backend_server.global.security.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private RestClient restClient;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private JwtProperties jwtProperties;

    // RestClient의 플루언트 API를 Mocking하기 위한 추가 Mock 객체들
    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private RestClient.RequestBodySpec requestBodySpec;
    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private RestClient.ResponseSpec responseSpec;

    private final String KAKAO_CLIENT_ID = "testClientId";
    private final String KAKAO_CLIENT_SECRET = "testClientSecret";
    private final String KAKAO_REDIRECT_URI = "http://localhost:3000";
    private final String KAKAO_TOKEN_URI = "https://kauth.kakao.com/oauth/token";
    private final String KAKAO_USER_INFO_URI = "https://kapi.kakao.com/v2/user/me";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "kakaoClientId", KAKAO_CLIENT_ID);
        ReflectionTestUtils.setField(authService, "kakaoClientSecret", KAKAO_CLIENT_SECRET);
        ReflectionTestUtils.setField(authService, "kakaoRedirectUri", KAKAO_REDIRECT_URI);
        ReflectionTestUtils.setField(authService, "kakaoTokenUri", KAKAO_TOKEN_URI);
        ReflectionTestUtils.setField(authService, "kakaoUserInfoUri", KAKAO_USER_INFO_URI);
    }

    @DisplayName("Access Token 재발급 성공")
    @Test
    void reissue_success() {
        // given
        String bearerToken = "Bearer valid-refresh-token";
        String refreshToken = "valid-refresh-token";
        Long userId = 1L;
        String newAccessToken = "new-access-token";

        Claims claims = Jwts.claims().setSubject(userId.toString());
        claims.put("tokenType", "REFRESH");

        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);

        RefreshToken storedRefreshToken = new RefreshToken(userId, refreshToken, 3600L);

        given(jwtProvider.extractToken(bearerToken)).willReturn(refreshToken);
        given(jwtProvider.verify(refreshToken)).willReturn(claims);
        given(refreshTokenRepository.findById(userId)).willReturn(Optional.of(storedRefreshToken));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(jwtProvider.createAccessToken(user)).willReturn(newAccessToken);

        // when
        ReissueResponseDto result = authService.reissue(bearerToken);

        // then
        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo(newAccessToken);
    }

    @DisplayName("Access Token으로 재발급 요청 시 예외 발생")
    @Test
    void reissue_withWrongTokenType_throwsException() {
        // given
        String bearerToken = "Bearer access-token";
        String accessToken = "access-token";
        Long userId = 1L;

        Claims claims = Jwts.claims().setSubject(userId.toString());
        claims.put("tokenType", "ACCESS"); // 토큰 타입이 ACCESS

        given(jwtProvider.extractToken(bearerToken)).willReturn(accessToken);
        given(jwtProvider.verify(accessToken)).willReturn(claims);

        // when & then
        assertThatThrownBy(() -> authService.reissue(bearerToken))
                .isInstanceOf(NotRefreshTokenException.class);
    }

    @DisplayName("Redis에 저장되지 않은 토큰으로 재발급 요청 시 예외 발생")
    @Test
    void reissue_withLoggedOutToken_throwsException() {
        // given
        String bearerToken = "Bearer logged-out-token";
        String refreshToken = "logged-out-token";
        Long userId = 1L;

        Claims claims = Jwts.claims().setSubject(userId.toString());
        claims.put("tokenType", "REFRESH");

        given(jwtProvider.extractToken(bearerToken)).willReturn(refreshToken);
        given(jwtProvider.verify(refreshToken)).willReturn(claims);
        given(refreshTokenRepository.findById(userId)).willReturn(Optional.empty()); // Redis에 토큰이 없음

        // when & then
        assertThatThrownBy(() -> authService.reissue(bearerToken))
                .isInstanceOf(LoggedOutUserException.class);
    }

    @DisplayName("Redis의 토큰과 불일치하는 토큰으로 재발급 요청 시 예외 발생")
    @Test
    void reissue_withMismatchedToken_throwsException() {
        // given
        String bearerToken = "Bearer valid-but-mismatched-token";
        String refreshToken = "valid-but-mismatched-token";
        Long userId = 1L;

        Claims claims = Jwts.claims().setSubject(userId.toString());
        claims.put("tokenType", "REFRESH");

        // Redis에는 다른 토큰이 저장되어 있는 상황
        RefreshToken storedRefreshToken = new RefreshToken(userId, "stored-but-different-token", 3600L);

        given(jwtProvider.extractToken(bearerToken)).willReturn(refreshToken);
        given(jwtProvider.verify(refreshToken)).willReturn(claims);
        given(refreshTokenRepository.findById(userId)).willReturn(Optional.of(storedRefreshToken));

        // when & then
        assertThatThrownBy(() -> authService.reissue(bearerToken))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @DisplayName("카카오 로그인 - 토큰 요청 타임아웃 시 예외 발생")
    @Test
    void kakaoLogin_timeout_throwsException() {
        // given
        String authorizationCode = "testAuthCode";

        // Mocking: 카카오 토큰 요청 시 타임아웃(ResourceAccessException) 발생
        given(restClient.post()).willReturn(requestBodyUriSpec);
        given(requestBodyUriSpec.uri(KAKAO_TOKEN_URI)).willReturn(requestBodySpec);
        given(requestBodySpec.contentType(any())).willReturn(requestBodySpec);
        given(requestBodySpec.body(any(MultiValueMap.class))).willReturn(requestBodySpec);
        given(requestBodySpec.retrieve()).willThrow(new ResourceAccessException("I/O error: Read timed out"));

        // when & then
        assertThatThrownBy(() -> authService.kakaoLogin(authorizationCode))
                .isInstanceOf(KakaoApiTimeoutException.class);
    }

    @DisplayName("기존 회원 카카오 로그인 성공")
    @Test
    void kakaoLogin_existingUser_success() {
        // given
        String authorizationCode = "testAuthCode";
        Long kakaoId = 12345L;
        String nickname = "testUser";
        String accessToken = "testAccessToken";
        String refreshToken = "testRefreshToken";

        // 1. Mocking: 카카오 토큰 응답
        KakaoTokenResponseDto mockTokenResponse = new KakaoTokenResponseDto();
        mockTokenResponse.setAccessToken("kakaoAccessToken");
        given(restClient.post()).willReturn(requestBodyUriSpec);
        given(requestBodyUriSpec.uri(KAKAO_TOKEN_URI)).willReturn(requestBodySpec);
        given(requestBodySpec.contentType(any())).willReturn(requestBodySpec);
        given(requestBodySpec.body(any(MultiValueMap.class))).willReturn(requestBodySpec);
        given(requestBodySpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.body(KakaoTokenResponseDto.class)).willReturn(mockTokenResponse);

        // 2. Mocking: 카카오 사용자 정보 응답 (더 구체적인 조건으로 수정)
        KakaoUserInfoResponseDto mockUserInfoResponse = new KakaoUserInfoResponseDto();
        mockUserInfoResponse.setId(kakaoId);
        KakaoUserInfoResponseDto.Properties properties = new KakaoUserInfoResponseDto.Properties();
        properties.setNickname(nickname);
        mockUserInfoResponse.setProperties(properties);
        given(restClient.get()).willReturn(requestHeadersUriSpec);
        given(requestHeadersUriSpec.uri(KAKAO_USER_INFO_URI)).willReturn(requestHeadersSpec);
        given(requestHeadersSpec.header(any(), any())).willReturn(requestHeadersSpec); // header() 호출을 모두 처리
        given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.body(KakaoUserInfoResponseDto.class)).willReturn(mockUserInfoResponse);

        // 3. Mocking: UserRepository - 기존 사용자 존재
        User existingUser = User.builder()
                .kakaoId(kakaoId)
                .name(nickname)
                .email("test@example.com")
                .studentId("123456")
                .phoneNumber("01012345678")
                .department("컴퓨터공학과")
                .build();
        when(userRepository.findByKakaoId(kakaoId)).thenReturn(Optional.of(existingUser));

        // 4. Mocking: JwtProvider - 토큰 생성
        when(jwtProvider.createAccessToken(existingUser)).thenReturn(accessToken);
        when(jwtProvider.createRefreshToken(existingUser)).thenReturn(refreshToken);

        // when
        LoginResponse result = authService.kakaoLogin(authorizationCode);

        // then
        assertThat(result).isInstanceOf(LoginSuccessResponseDto.class);
        LoginSuccessResponseDto successResponse = (LoginSuccessResponseDto) result;
        assertThat(successResponse.status()).isEqualTo(AuthStatus.LOGIN_SUCCESS);
        assertThat(successResponse.accessToken()).isEqualTo(accessToken);
        assertThat(successResponse.refreshToken()).isEqualTo(refreshToken);
    }

    @DisplayName("신규 회원 카카오 로그인 - 추가 정보 입력 필요")
    @Test
    void kakaoLogin_newUser_registrationRequired() {
        // given
        String authorizationCode = "testAuthCode";
        Long kakaoId = 54321L;
        String nickname = "newbie";
        String temporaryToken = "testTemporaryToken";

        // 1. Mocking: 카카오 토큰 응답
        KakaoTokenResponseDto mockTokenResponse = new KakaoTokenResponseDto();
        mockTokenResponse.setAccessToken("kakaoAccessToken");
        given(restClient.post()).willReturn(requestBodyUriSpec);
        given(requestBodyUriSpec.uri(KAKAO_TOKEN_URI)).willReturn(requestBodySpec);
        given(requestBodySpec.contentType(any())).willReturn(requestBodySpec);
        given(requestBodySpec.body(any(MultiValueMap.class))).willReturn(requestBodySpec);
        given(requestBodySpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.body(KakaoTokenResponseDto.class)).willReturn(mockTokenResponse);

        // 2. Mocking: 카카오 사용자 정보 응답
        KakaoUserInfoResponseDto mockUserInfoResponse = new KakaoUserInfoResponseDto();
        mockUserInfoResponse.setId(kakaoId);
        KakaoUserInfoResponseDto.Properties properties = new KakaoUserInfoResponseDto.Properties();
        properties.setNickname(nickname);
        mockUserInfoResponse.setProperties(properties);
        given(restClient.get()).willReturn(requestHeadersUriSpec);
        given(requestHeadersUriSpec.uri(KAKAO_USER_INFO_URI)).willReturn(requestHeadersSpec);
        given(requestHeadersSpec.header(any(), any())).willReturn(requestHeadersSpec);
        given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.body(KakaoUserInfoResponseDto.class)).willReturn(mockUserInfoResponse);

        // 3. Mocking: UserRepository - 신규 사용자 (존재하지 않음)
        when(userRepository.findByKakaoId(kakaoId)).thenReturn(Optional.empty());

        // 4. Mocking: JwtProvider - 임시 토큰 생성
        when(jwtProvider.createTemporaryToken(kakaoId, nickname)).thenReturn(temporaryToken);

        // when
        LoginResponse result = authService.kakaoLogin(authorizationCode);

        // then
        assertThat(result).isInstanceOf(RegistrationRequiredResponseDto.class);
        RegistrationRequiredResponseDto registrationResponse = (RegistrationRequiredResponseDto) result;
        assertThat(registrationResponse.status()).isEqualTo(AuthStatus.REGISTRATION_REQUIRED);
        assertThat(registrationResponse.temporaryToken()).isEqualTo(temporaryToken);
    }

    @DisplayName("신규 회원 가입 성공")
    @Test
    void register_newUser_success() {
        // given
        String bearerToken = "Bearer testTemporaryToken";
        String temporaryToken = "testTemporaryToken";
        Long kakaoId = 12345L;
        String studentId = "newStudent123";

        RegisterRequestDto requestDto = new RegisterRequestDto(
                "newUser", "new@example.com", studentId, "컴퓨터공학과", "01011112222"
        );

        Claims claims = Jwts.claims();
        claims.setSubject("temporary");
        claims.put("kakaoId", kakaoId);

        given(jwtProvider.extractToken(bearerToken)).willReturn(temporaryToken);
        given(jwtProvider.verify(temporaryToken)).willReturn(claims);
        given(userRepository.findByStudentId(studentId)).willReturn(Optional.empty());
        given(jwtProvider.createAccessToken(any(User.class))).willReturn("newAccessToken");
        given(jwtProvider.createRefreshToken(any(User.class))).willReturn("newRefreshToken");

        // when
        LoginSuccessResponseDto result = authService.register(bearerToken, requestDto);

        // then
        assertThat(result.status()).isEqualTo(AuthStatus.REGISTER_SUCCESS);
        assertThat(result.accessToken()).isEqualTo("newAccessToken");
        assertThat(result.refreshToken()).isEqualTo("newRefreshToken");
    }

    @DisplayName("기존 사용자 계정 연결 성공")
    @Test
    void register_linkAccount_success() {
        // given
        String bearerToken = "Bearer testTemporaryToken";
        String temporaryToken = "testTemporaryToken";
        Long kakaoId = 54321L;
        String studentId = "existingStudent456";

        RegisterRequestDto requestDto = new RegisterRequestDto(
                "existingUser", "existing@example.com", studentId, "전자공학과", "01033334444"
        );

        User existingUser = User.builder()
                .kakaoId(null) // 카카오 ID가 없는 상태
                .studentId(studentId)
                .name("기존사용자")
                .email("pre-existing@example.com")
                .department("전자공학과")
                .phoneNumber("01033334444")
                .build();

        Claims claims = Jwts.claims();
        claims.setSubject("temporary");
        claims.put("kakaoId", kakaoId);

        given(jwtProvider.extractToken(bearerToken)).willReturn(temporaryToken);
        given(jwtProvider.verify(temporaryToken)).willReturn(claims);
        given(userRepository.findByStudentId(studentId)).willReturn(Optional.of(existingUser));
        given(jwtProvider.createAccessToken(any(User.class))).willReturn("linkedAccessToken");
        given(jwtProvider.createRefreshToken(any(User.class))).willReturn("linkedRefreshToken");

        // when
        LoginSuccessResponseDto result = authService.register(bearerToken, requestDto);

        // then
        assertThat(result.status()).isEqualTo(AuthStatus.REGISTER_SUCCESS);
        assertThat(existingUser.getKakaoId()).isEqualTo(kakaoId); // kakaoId가 연결되었는지 확인
        assertThat(result.accessToken()).isEqualTo("linkedAccessToken");
        assertThat(result.refreshToken()).isEqualTo("linkedRefreshToken");
    }

    @DisplayName("계정 탈취 시도 - 이미 연동된 학번으로 가입 시 예외 발생")
    @Test
    void register_duplicateKakaoId_throwsException() {
        // given
        String bearerToken = "Bearer testTemporaryToken";
        String temporaryToken = "testTemporaryToken";
        Long attackerKakaoId = 99999L; // 공격자의 카카오 ID
        String victimStudentId = "victimStudent789"; // 이미 가입된 피해자의 학번

        RegisterRequestDto requestDto = new RegisterRequestDto(
                "attacker", "attacker@example.com", victimStudentId, "해킹학과", "01055556666"
        );

        User victimUser = User.builder()
                .kakaoId(11111L) // 피해자는 이미 다른 카카오 ID로 연동되어 있음
                .studentId(victimStudentId)
                .name("피해자")
                .email("victim@example.com")
                .department("경영학과")
                .phoneNumber("01077778888")
                .build();

        Claims claims = Jwts.claims();
        claims.setSubject("temporary"); // subject를 "temporary"로 설정
        claims.put("kakaoId", attackerKakaoId); // kakaoId 클레임 추가

        given(jwtProvider.extractToken(bearerToken)).willReturn(temporaryToken);
        given(jwtProvider.verify(temporaryToken)).willReturn(claims);
        given(userRepository.findByStudentId(victimStudentId)).willReturn(Optional.of(victimUser));

        // when & then
        assertThatThrownBy(() -> authService.register(bearerToken, requestDto))
                .isInstanceOf(DuplicateKakaoIdException.class)
                .hasMessage("이미 다른 계정과 연동된 학번입니다.");
    }
}
