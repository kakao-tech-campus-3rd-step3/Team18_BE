package com.kakaotech.team18.backend_server.domain.auth.service;

import com.kakaotech.team18.backend_server.domain.auth.dto.LoginResponse;

public interface AuthService {

    /**
     * 카카오 서버로부터 받은 인가 코드를 사용하여 로그인 또는 회원가입을 처리합니다.
     *
     * @param authorizationCode 카카오 서버로부터 받은 인가 코드
     * @return 로그인 성공 시 LoginSuccessResponseDto, 추가 정보 입력 필요 시 RegistrationRequiredResponseDto를 반환합니다.
     *         두 DTO는 모두 LoginResponse 인터페이스를 구현합니다.
     */
    LoginResponse kakaoLogin(String authorizationCode);

    // TODO: 추가 정보 입력을 위한 회원가입 완료 메서드는 다음 단계에서 추가
    // void register(RegisterRequestDto registerRequestDto, Long userId);
}
