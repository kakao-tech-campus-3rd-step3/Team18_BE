package com.kakaotech.team18.backend_server.domain.auth.service;

import com.kakaotech.team18.backend_server.domain.auth.dto.*;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.domain.user.repository.UserRepository;
import com.kakaotech.team18.backend_server.global.exception.exceptions.DuplicateKakaoIdException;
import com.kakaotech.team18.backend_server.global.security.JwtProvider; // 경로 수정
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RestTemplate restTemplate;

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

            return new LoginSuccessResponseDto("LOGIN_SUCCESS", accessToken, refreshToken);
        } else {
            // 4-2. 신규 회원일 경우: 추가 정보 입력 필요
            log.info("신규 회원, 추가 정보 입력 필요");

            // 임시 토큰 발급
            String temporaryToken = jwtProvider.createTemporaryToken(
                    kakaoUserInfo.getId(),
                    kakaoUserInfo.getProperties().getNickname()
            );

            return new RegistrationRequiredResponseDto("REGISTRATION_REQUIRED", temporaryToken);
        }
    }

    @Override
    @Transactional
    public LoginSuccessResponseDto register(String bearerToken, RegisterRequestDto registerRequestDto) {
        // 1. 임시 토큰 검증 및 정보 추출
        String temporaryToken = jwtProvider.extractToken(bearerToken);
        Claims claims = jwtProvider.verify(temporaryToken);
        Long kakaoId = claims.get("kakaoId", Long.class);

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
                throw new DuplicateKakaoIdException("이미 연동된 계정이 존재합니다.");
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

        // 4. 정식 토큰 발급
        String accessToken = jwtProvider.createAccessToken(user);
        String refreshToken = jwtProvider.createRefreshToken(user);

        return new LoginSuccessResponseDto("REGISTER_SUCCESS", accessToken, refreshToken);
    }

    private KakaoTokenResponseDto getKakaoAccessToken(String authorizationCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("client_secret", kakaoClientSecret);
        params.add("redirect_uri", kakaoRedirectUri);
        params.add("code", authorizationCode);

        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(params, headers);

        ResponseEntity<KakaoTokenResponseDto> response = restTemplate.exchange(
                kakaoTokenUri,
                HttpMethod.POST,
                kakaoTokenRequest,
                KakaoTokenResponseDto.class
        );

        return response.getBody();
    }

    private KakaoUserInfoResponseDto getKakaoUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);

        ResponseEntity<KakaoUserInfoResponseDto> response = restTemplate.exchange(
                kakaoUserInfoUri,
                HttpMethod.GET,
                kakaoUserInfoRequest,
                KakaoUserInfoResponseDto.class
        );

        return response.getBody();
    }
}
