package com.kakaotech.team18.backend_server.domain.auth.service;

import com.kakaotech.team18.backend_server.domain.auth.dto.AuthStatus;
import com.kakaotech.team18.backend_server.domain.auth.dto.KakaoTokenResponseDto;
import com.kakaotech.team18.backend_server.domain.auth.dto.KakaoUserInfoResponseDto;
import com.kakaotech.team18.backend_server.domain.auth.dto.LoginResponse;
import com.kakaotech.team18.backend_server.domain.auth.dto.LoginSuccessResponseDto;
import com.kakaotech.team18.backend_server.domain.auth.dto.RegisterRequestDto;
import com.kakaotech.team18.backend_server.domain.auth.dto.RegistrationRequiredResponseDto;
import com.kakaotech.team18.backend_server.domain.auth.dto.ReissueResponseDto;
import com.kakaotech.team18.backend_server.domain.auth.entity.RefreshToken;
import com.kakaotech.team18.backend_server.domain.auth.repository.RefreshTokenRepository;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubIdAndRoleInfoDto;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import com.kakaotech.team18.backend_server.domain.clubMember.repository.ClubMemberRepository;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.domain.user.repository.UserRepository;
import com.kakaotech.team18.backend_server.global.exception.exceptions.DuplicateKakaoIdException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.InvalidRefreshTokenException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.KakaoApiException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.KakaoApiTimeoutException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.LoggedOutUserException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.NotRefreshTokenException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.UnauthenticatedUserException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.UserNotFoundException;
import com.kakaotech.team18.backend_server.global.security.JwtProperties;
import com.kakaotech.team18.backend_server.global.security.JwtProvider;
import com.kakaotech.team18.backend_server.global.security.TokenType;
import io.jsonwebtoken.Claims;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RestClient restClient;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;
    private final ClubMemberRepository clubMemberRepository;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String kakaoClientSecret;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String kakaoTokenUri;

    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String kakaoUserInfoUri;

    @Override
    @Transactional
    public LoginResponse kakaoLogin(String authorizationCode) {
        // 1. 인가 코드로 카카오 서버에 Access Token 요청
        KakaoTokenResponseDto kakaoTokenResponse = this.getKakaoAccessToken(authorizationCode);
        log.info("카카오 Access Token 받아오기 성공");

        // 2. 카카오 Access Token으로 사용자 정보(kakaoId 등) 요청
        KakaoUserInfoResponseDto kakaoUserInfo = this.getKakaoUserInfo(kakaoTokenResponse.getAccessToken());
        log.info("카카오 사용자 정보 받아오기 성공: {}", kakaoUserInfo.getId());

        // 3. 받아온 kakaoId로 우리 DB 조회
        Optional<User> userOptional = userRepository.findByKakaoId(kakaoUserInfo.getId());

        // 4. 조회 결과에 따라 분기 처리 (기존/신규)
        if (userOptional.isPresent()) {
            // 4-1. 기존 회원일 경우: 로그인 성공 처리
            User user = userOptional.get();
            log.info("기존 회원 로그인: {}", user.getId());

            // 정식 토큰 발급
            String accessToken = jwtProvider.createAccessToken(user);
            String refreshToken = jwtProvider.createRefreshToken(user);

            // Redis에 Refresh Token 저장
            refreshTokenRepository.save(new RefreshToken(user.getId(), refreshToken, jwtProperties.refreshTokenValidityInSeconds()));
            log.info("Redis에 Refresh Token 저장 완료: userId={}", user.getId());

            //clubId, Role 전달
            List<ClubIdAndRoleInfoDto> clubIdAndRoleList = clubMemberRepository.findClubIdAndRoleByUser(user);

            //서비스 관리자가 로그인하는 경우
            if (clubIdAndRoleList.isEmpty() && isSystemAdmin(user)) {
                clubIdAndRoleList = List.of(new ClubIdAndRoleInfoDto(null, Role.SYSTEM_ADMIN));
            }

            return new LoginSuccessResponseDto(AuthStatus.LOGIN_SUCCESS, accessToken, refreshToken, clubIdAndRoleList);
        } else {
            // 4-2. 신규 회원일 경우: 추가 정보 입력 필요
            log.info("신규 회원, 추가 정보 입력 필요");

            // 임시 토큰 발급
            String temporaryToken = jwtProvider.createTemporaryToken(
                    kakaoUserInfo.getId(),
                    kakaoUserInfo.getProperties().getNickname()
            );

            return new RegistrationRequiredResponseDto(AuthStatus.REGISTRATION_REQUIRED, temporaryToken);
        }
    }

    @Override
    @Transactional
    public LoginSuccessResponseDto register(String bearerToken, RegisterRequestDto registerRequestDto) {
        // 1. 임시 토큰 검증 및 정보 추출
        String temporaryToken = jwtProvider.extractToken(bearerToken);
        Claims claims = jwtProvider.verify(temporaryToken);

        // 1-1. 토큰 타입 검증: 이 토큰이 '임시 토큰'이 맞는지 확인
        if (!TokenType.TEMPORARY.name().equals(claims.getSubject())) {
            throw new UnauthenticatedUserException("회원가입에는 임시 토큰이 필요합니다.");
        }

        // 1-2. 필수 정보 검증: 토큰에 'kakaoId'가 포함되어 있는지 확인
        Long kakaoId = claims.get("kakaoId", Long.class);
        if (kakaoId == null) {
            throw new UnauthenticatedUserException("토큰에 필수 정보(kakaoId)가 없습니다.");
        }

        // 2. 학번으로 기존 사용자가 있는지 조회
        Optional<User> userOptional = userRepository.findByStudentId(registerRequestDto.studentId());

        User user;
        if (userOptional.isPresent()) {
            // 3-1. 계정 연결: 학번으로 사용자가 존재하면
            user = userOptional.get();

            // 보안 강화: 해당 학번의 사용자가 이미 카카오 연동이 되어 있는지 확인
            if (user.getKakaoId() != null) {
                // 이미 연동된 계정이 있다면, 계정 탈취 시도일 수 있으므로 에러 발생
                log.warn("계정 연결 시도 실패: 이미 카카오 계정과 연동된 학번입니다. studentId={}", registerRequestDto.studentId());
                throw new DuplicateKakaoIdException("이미 다른 계정과 연동된 학번입니다.");
            }

            log.info("기존 사용자 계정 연결: studentId={}, kakaoId={}", user.getStudentId(), kakaoId);
            user.connectKakaoId(kakaoId);
        } else {
            // 3-2. 신규 생성: 학번으로 사용자가 없으면, 새로운 User 생성
            log.info("신규 사용자 생성: kakaoId={}", kakaoId);
            user = User.builder()
                    .kakaoId(kakaoId)
                    .name(registerRequestDto.name())
                    .email(registerRequestDto.email())
                    .studentId(registerRequestDto.studentId())
                    .phoneNumber(registerRequestDto.phoneNumber())
                    .department(registerRequestDto.department())
                    .build();
            userRepository.save(user);
        }

        //clubId, Role 전달
        List<ClubIdAndRoleInfoDto> clubIdAndRoleList = clubMemberRepository.findClubIdAndRoleByUser(user);

        //서비스 관리자가 로그인하는 경우
        if (clubIdAndRoleList.isEmpty() && isSystemAdmin(user)) {
            clubIdAndRoleList = List.of(new ClubIdAndRoleInfoDto(null, Role.SYSTEM_ADMIN));
        }

        // 4. 정식 토큰 발급
        String accessToken = jwtProvider.createAccessToken(user);
        String refreshToken = jwtProvider.createRefreshToken(user);

        // Redis에 Refresh Token 저장
        refreshTokenRepository.save(new RefreshToken(user.getId(), refreshToken, jwtProperties.refreshTokenValidityInSeconds()));
        log.info("Redis에 Refresh Token 저장 완료: userId={}", user.getId());

        return new LoginSuccessResponseDto(AuthStatus.REGISTER_SUCCESS, accessToken, refreshToken, clubIdAndRoleList);
    }

    @Override
    @Transactional
    public ReissueResponseDto reissue(String refreshToken) {
        // 1. Refresh Token 자체 유효성 검증 (만료, 서명 등)
        Claims claims = jwtProvider.verify(refreshToken);

        // 2. 토큰 타입 검증
        String tokenType = claims.get("tokenType", String.class);
        if (!TokenType.REFRESH.name().equals(tokenType)) {
            log.warn("Refresh Token 재발급 시도 실패: 토큰 타입이 REFRESH가 아님");
            throw new NotRefreshTokenException();
        }

        // 3. 사용자 ID 추출
        Long userId = Long.valueOf(claims.getSubject());

        // 4. Redis에 저장된 토큰과 일치하는지 검증
        RefreshToken storedRefreshToken = refreshTokenRepository.findById(userId)
                .orElseThrow(LoggedOutUserException::new);

        if (!storedRefreshToken.getRefreshToken().equals(refreshToken)) {
            log.warn("Refresh Token 재발급 시도 실패: Redis에 저장된 토큰과 불일치. userId={}", userId);
            throw new InvalidRefreshTokenException();
        }

        // 5. 사용자 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("해당 유저가 존재하지 않습니다."));

        // 6. 새로운 토큰 발급 (Access, Refresh 둘 다)
        String newAccessToken = jwtProvider.createAccessToken(user);
        String newRefreshToken = jwtProvider.createRefreshToken(user);
        log.info("Access & Refresh Token 재발급 성공: userId={}", userId);

        // 7. Redis에 새로운 Refresh Token 덮어쓰기 (Rotation)
        refreshTokenRepository.save(new RefreshToken(user.getId(), newRefreshToken, jwtProperties.refreshTokenValidityInSeconds()));
        log.info("Redis에 새로운 Refresh Token 저장(덮어쓰기) 완료: userId={}", user.getId());

        // 8. DTO로 감싸서 반환
        return ReissueResponseDto.of(newAccessToken, newRefreshToken);
    }

    private KakaoTokenResponseDto getKakaoAccessToken(String authorizationCode) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("client_secret", kakaoClientSecret);
        params.add("redirect_uri", kakaoRedirectUri);
        params.add("code", authorizationCode);

        try {
            return restClient.post()
                    .uri(kakaoTokenUri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(params)
                    .retrieve()
                    .body(KakaoTokenResponseDto.class);
        } catch (ResourceAccessException e) {
            log.warn("카카오 Access Token 요청 중 타임아웃 발생", e);
            throw new KakaoApiTimeoutException();
        } catch (RestClientResponseException e) {
            log.warn("카카오 Access Token 요청 실패: " + e.getResponseBodyAsString(), e);
            throw new KakaoApiException();
        }
    }

    private KakaoUserInfoResponseDto getKakaoUserInfo(String accessToken) {
        try {
            return restClient.get()
                    .uri(kakaoUserInfoUri)
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-type", "application/x-www-form-urlencoded;charset=utf-8")
                    .retrieve()
                    .body(KakaoUserInfoResponseDto.class);
        } catch (ResourceAccessException e) {
            log.warn("카카오 사용자 정보 요청 중 타임아웃 발생", e);
            throw new KakaoApiTimeoutException();
        } catch (RestClientResponseException e) {
            log.warn("카카오 사용자 정보 요청 실패: " + e.getResponseBodyAsString(), e);
            throw new KakaoApiException();
        }
    }

    private boolean isSystemAdmin(User user) {
        return clubMemberRepository.findByUser(user).stream()
                .anyMatch(cm -> cm.getRole() == Role.SYSTEM_ADMIN);
    }
}
