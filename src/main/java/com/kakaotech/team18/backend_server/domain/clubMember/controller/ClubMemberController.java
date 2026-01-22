package com.kakaotech.team18.backend_server.domain.clubMember.controller;

import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberResponseDto;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberSaveRequestDto;
import com.kakaotech.team18.backend_server.domain.clubMember.service.ClubMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "동아리원 관리 API", description = "동아리원 조회, 등록, 수정, 삭제 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clubs")
public class ClubMemberController {

    private final ClubMemberService clubMemberService;

    @Operation(summary = "동아리원 목록 조회", description = "특정 동아리의 전체 동아리원 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (회장/운영진만 가능)"),
            @ApiResponse(responseCode = "404", description = "해당 동아리를 찾을 수 없음")
    })
    @PreAuthorize("@customSecurityService.isClubAdminOrExecutive(#clubId)")
    @GetMapping("/{clubId}/members")
    public ResponseEntity<List<ClubMemberResponseDto>> getClubMembers(
            @Parameter(description = "동아리 ID", required = true, example = "1") @PathVariable Long clubId
    ) {
        List<ClubMemberResponseDto> response = clubMemberService.getClubMembers(clubId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "동아리원 수동 등록 (단건)", description = "동아리원을 한 명씩 수동으로 등록하거나 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "등록/수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력값"),
            @ApiResponse(responseCode = "409", description = "중복 등록 실패 (이름 불일치)"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (회장/운영진만 가능)")
    })
    @PreAuthorize("@customSecurityService.isClubAdminOrExecutive(#clubId)")
    @PostMapping("/{clubId}/members")
    public ResponseEntity<ClubMemberResponseDto> registerMember(
            @Parameter(description = "동아리 ID", required = true, example = "1") @PathVariable Long clubId,
            @Valid @RequestBody ClubMemberSaveRequestDto requestDto
    ) {
        ClubMemberResponseDto response = clubMemberService.registerMember(clubId, requestDto);
        return ResponseEntity.ok(response);
    }
}
