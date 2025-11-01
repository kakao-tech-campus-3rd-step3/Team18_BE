package com.kakaotech.team18.backend_server.domain.auth.service;

import com.kakaotech.team18.backend_server.domain.auth.dto.LoginResponse;
import com.kakaotech.team18.backend_server.domain.auth.dto.LoginSuccessResponseDto;
import com.kakaotech.team18.backend_server.domain.auth.dto.RegisterRequestDto;
import com.kakaotech.team18.backend_server.domain.auth.dto.ReissueResponseDto;

import jakarta.servlet.http.HttpServletResponse;
public interface AuthService {

    /**
     * 카카오 서버로부터 받은 인가 코드를 사용하여 로그인 또는 회원가입을 처리합니다.
     *
     * @param authorizationCode 카카오 서버로부터 받은 인가 코드
     * @return 로그인 성공 시 LoginSuccessResponseDto, 추가 정보 입력 필요 시 RegistrationRequiredResponseDto를 반환합니다.
     *         두 DTO는 모두 LoginResponse 인터페이스를 구현합니다.
     */
    LoginResponse kakaoLogin(String authorizationCode);

    /**
     * 신규 회원이 제출한 추가 정보와 임시 토큰을 사용하여 최종 회원가입을 완료하고, 정식 토큰을 발급합니다.
     *
     * @param temporaryToken     신규 회원임을 증명하는 임시 토큰
     * @param registerRequestDto 사용자가 입력한 추가 정보(이름, 학번, 학과 등)
     * @return 회원가입 및 로그인 성공 결과로, 정식 토큰이 담긴 DTO
     */
    LoginSuccessResponseDto register(String temporaryToken, RegisterRequestDto registerRequestDto);

    /**
     * 유효한 Refresh Token을 사용하여 만료된 Access Token을 재발급합니다.
     *
     * @param bearerToken "Bearer " 접두사를 포함한 Refresh Token
     * @return 새로 발급된 Access Token이 담긴 DTO
     */
    ReissueResponseDto reissue(String bearerToken);

    /**
     * 사용자의 Access Token을 블랙리스트에 추가하고, Refresh Token을 무효화하여 로그아웃 처리합니다.
     *
     * @param bearerToken "Bearer " 접두사를 포함한 Access Token
     * @param response    HttpOnly 쿠키에 저장된 Refresh Token을 만료시키기 위한 HttpServletResponse
     */
    void logout(String bearerToken, HttpServletResponse response);

}
