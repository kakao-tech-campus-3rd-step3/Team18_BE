package com.kakaotech.team18.backend_server.domain.auth.controller;

import com.kakaotech.team18.backend_server.domain.auth.dto.KakaoLoginRequestDto;
import com.kakaotech.team18.backend_server.domain.auth.dto.LoginResponse;
import com.kakaotech.team18.backend_server.domain.auth.dto.LoginSuccessResponseDto;
import com.kakaotech.team18.backend_server.domain.auth.dto.RegistrationRequiredResponseDto;
import com.kakaotech.team18.backend_server.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증 API", description = "카카오 소셜 로그인 및 회원가입 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService; // 실제 로직을 처리할 서비스

    @Operation(summary = "카카오 인가 코드로 로그인/회원가입", description = "클라이언트가 카카오로부터 받은 인가 코드를 전송하면, 서버는 이를 검증하여 기존 회원이면 즉시 로그인 처리하고, 신규 회원이면 추가 정보 입력을 위한 임시 토큰을 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공 또는 추가 정보 입력 필요",
                    content = @Content(schema = @Schema(oneOf = {LoginSuccessResponseDto.class, RegistrationRequiredResponseDto.class})))
    })
    @PostMapping("/kakao/login")
    public ResponseEntity<LoginResponse> kakaoLogin(@Valid @RequestBody KakaoLoginRequestDto kakaoLoginRequestDto) {
        // 서비스 레이어에 인가 코드를 전달하고, 그 결과를 LoginResponse 타입으로 받습니다.
        LoginResponse loginResponse = authService.kakaoLogin(kakaoLoginRequestDto.authorizationCode());
        return ResponseEntity.ok(loginResponse);
    }

    // TODO: 추가 정보 입력을 위한 'POST /api/auth/register' 엔드포인트는 다음 단계에서 구현
}
