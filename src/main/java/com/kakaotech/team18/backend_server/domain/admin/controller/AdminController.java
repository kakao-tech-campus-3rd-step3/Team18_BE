package com.kakaotech.team18.backend_server.domain.admin.controller;

import com.kakaotech.team18.backend_server.domain.admin.dto.LinkClubRequestDto;
import com.kakaotech.team18.backend_server.domain.admin.service.AdminService;
import com.kakaotech.team18.backend_server.global.dto.SuccessResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "System Admin", description = "system_admin 전용 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    @Operation(
            summary = "동아리장/임원과 동아리 연결",
            description = """
                    system_admin 계정이 학번, 동아리 이름, 직책(Role)을 입력해서
                    club_member 레코드를 생성합니다.

                    - 학번으로 User 조회 (없으면 InvalidStudentIdException)
                    - 동아리명으로 Club 조회 (없으면 InvalidClubNameException)
                    - 동일 User/Club/Role/ACTIVE 이미 존재 시 DuplicateClubMemberException
                    - 회장(CLUB_ADMIN)인데 이미 해당 클럽에 회장이 있으면 DuplicateClubAdminException
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "동아리장/임원 연결 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 입력 값 (InvalidStudentIdException, InvalidClubNameException)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 존재하는 동아리원/동아리 회장 (DuplicateClubMemberException, DuplicateClubAdminException)",
                    content = @Content
            )
    })
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @PostMapping("/linkClub")
    public ResponseEntity<SuccessResponseDto> linkClubPresident(
            @Valid @RequestBody LinkClubRequestDto requestDto
    ) {
        SuccessResponseDto response = adminService.linkClubPresident(requestDto);
        return ResponseEntity.ok(response);
    }
}