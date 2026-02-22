package com.kakaotech.team18.backend_server.domain.user.controller;

import com.kakaotech.team18.backend_server.domain.user.dto.MyProfileResponseDto;
import com.kakaotech.team18.backend_server.domain.user.dto.UserActivityDto;
import com.kakaotech.team18.backend_server.domain.user.service.UserService;
import com.kakaotech.team18.backend_server.global.security.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User API", description = "User Profile and Activity API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get My Profile", description = "Retrieves the logged-in user's profile.")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/profile")
    public ResponseEntity<MyProfileResponseDto> getMyProfile(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long userId = Long.parseLong(principalDetails.getUsername());
        return ResponseEntity.ok(userService.getMyProfile(userId));
    }

    @Operation(summary = "Get My Activities", description = "Retrieves the logged-in user's activities (Reviews, Comments, Applications).")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/activities")
    public ResponseEntity<UserActivityDto> getMyActivities(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long userId = Long.parseLong(principalDetails.getUsername());
        return ResponseEntity.ok(userService.getMyActivities(userId));
    }
}
