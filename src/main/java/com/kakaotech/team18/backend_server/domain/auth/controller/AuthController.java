package com.kakaotech.team18.backend_server.domain.auth.controller;

import com.kakaotech.team18.backend_server.domain.auth.dto.KakaoLoginRequestDto;
import com.kakaotech.team18.backend_server.domain.auth.dto.LoginResponse;
import com.kakaotech.team18.backend_server.domain.auth.dto.LoginSuccessResponseDto;
import com.kakaotech.team18.backend_server.domain.auth.dto.RegisterRequestDto;
import com.kakaotech.team18.backend_server.domain.auth.dto.RegistrationRequiredResponseDto;
import com.kakaotech.team18.backend_server.domain.auth.dto.ReissueResponseDto;
import com.kakaotech.team18.backend_server.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증 API", description = "카카오 소셜 로그인 및 회원가입 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final long REFRESH_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 24 * 14; // 14일

    @Operation(summary = "카카오 인가 코드로 로그인/회원가입", description = "클라이언트가 카카오로부터 받은 인가 코드를 전송하면, 서버는 이를 검증하여 기존 회원이면 즉시 로그인 처리하고, 신규 회원이면 추가 정보 입력을 위한 임시 토큰을 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공 또는 추가 정보 입력 필요",
                    content = @Content(schema = @Schema(oneOf = {LoginSuccessResponseDto.Body.class, RegistrationRequiredResponseDto.class})))
    })
    @PostMapping("/kakao/login")
    public ResponseEntity<LoginResponse> kakaoLogin(@Valid @RequestBody KakaoLoginRequestDto kakaoLoginRequestDto) {
        LoginResponse loginResponse = authService.kakaoLogin(kakaoLoginRequestDto.authorizationCode());

        if (loginResponse instanceof LoginSuccessResponseDto dto) {
            ResponseCookie responseCookie = ResponseCookie.from("refreshToken", dto.refreshToken())
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(REFRESH_TOKEN_EXPIRE_TIME)
                    .build();

            LoginSuccessResponseDto.Body body = new LoginSuccessResponseDto.Body(dto.status(), dto.accessToken());

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                    .body(body);
        }

        return ResponseEntity.ok(loginResponse);
    }

    @Operation(summary = "추가 정보 제출 및 최종 회원가입", description = "임시 토큰과 함께 사용자의 추가 정보(학번, 학과 등)를 받아 최종적으로 회원가입을 완료하고, 정식 토큰을 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "회원가입 및 로그인 성공",
                    content = @Content(schema = @Schema(implementation = LoginSuccessResponseDto.Body.class))),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 임시 토큰")
    })
    @SecurityRequirement(name = "temporary-token") // Swagger에서 임시 토큰 헤더를 명시
    @PostMapping("/register")
    public ResponseEntity<LoginSuccessResponseDto.Body> register(
            @RequestHeader("Authorization") String temporaryToken,
            @Valid @RequestBody RegisterRequestDto registerRequestDto) {

        LoginSuccessResponseDto responseDto = authService.register(temporaryToken, registerRequestDto);

        ResponseCookie responseCookie = ResponseCookie.from("refreshToken", responseDto.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(REFRESH_TOKEN_EXPIRE_TIME)
                .build();

        LoginSuccessResponseDto.Body body = new LoginSuccessResponseDto.Body(responseDto.status(), responseDto.accessToken());

        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(body);
    }

    @Operation(summary = "Access Token 재발급", description = "유효한 Refresh Token을 사용하여 만료된 Access Token을 재발급받습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Access Token 재발급 성공",
                    content = @Content(schema = @Schema(implementation = ReissueResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 Refresh Token")
    })
    @SecurityRequirement(name = "bearer-jwt")
    @PostMapping("/reissue")
    public ResponseEntity<ReissueResponseDto> reissue(@RequestHeader("Authorization") String bearerToken) {
        ReissueResponseDto reissueResponseDto = authService.reissue(bearerToken);
        return ResponseEntity.ok(reissueResponseDto);
    }
}
