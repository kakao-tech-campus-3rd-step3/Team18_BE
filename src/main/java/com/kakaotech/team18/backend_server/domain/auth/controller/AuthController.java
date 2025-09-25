package com.kakaotech.team18.backend_server.domain.auth.controller;

import com.kakaotech.team18.backend_server.domain.auth.dto.KakaoLoginRequestDto;
import com.kakaotech.team18.backend_server.domain.auth.dto.LoginResponse;
import com.kakaotech.team18.backend_server.domain.auth.dto.LoginSuccessResponseDto;
import com.kakaotech.team18.backend_server.domain.auth.dto.RegisterRequestDto;
import com.kakaotech.team18.backend_server.domain.auth.dto.RegistrationRequiredResponseDto;
import com.kakaotech.team18.backend_server.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증 API", description = "카카오 소셜 로그인 및 회원가입 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "카카오 인가 코드로 로그인/회원가입", description = "클라이언트가 카카오로부터 받은 인가 코드를 전송하면, 서버는 이를 검증하여 기존 회원이면 즉시 로그인 처리하고, 신규 회원이면 추가 정보 입력을 위한 임시 토큰을 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공 또는 추가 정보 입력 필요",
                    content = @Content(schema = @Schema(oneOf = {LoginSuccessResponseDto.class, RegistrationRequiredResponseDto.class})))
    })
    @PostMapping("/kakao/login")
    public ResponseEntity<LoginResponse> kakaoLogin(@Valid @RequestBody KakaoLoginRequestDto kakaoLoginRequestDto) {
        LoginResponse loginResponse = authService.kakaoLogin(kakaoLoginRequestDto.authorizationCode());
        return ResponseEntity.ok(loginResponse);
    }

    @Operation(summary = "추가 정보 제출 및 최종 회원가입", description = "임시 토큰과 함께 사용자의 추가 정보(학번, 학과 등)를 받아 최종적으로 회원가입을 완료하고, 정식 토큰을 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "회원가입 및 로그인 성공",
                    content = @Content(schema = @Schema(implementation = LoginSuccessResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 임시 토큰")
    })
    @SecurityRequirement(name = "temporary-token") // Swagger에서 임시 토큰 헤더를 명시
    @PostMapping("/register")
    public ResponseEntity<LoginSuccessResponseDto> register(
            @RequestHeader("Authorization") String temporaryToken,
            @Valid @RequestBody RegisterRequestDto registerRequestDto) {

        LoginSuccessResponseDto responseDto = authService.register(temporaryToken, registerRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
}
