package com.kakaotech.team18.backend_server.domain.club.controller;

import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDashBoardResponseDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDetailRequestDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDetailResponseDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubListResponseDto;
import com.kakaotech.team18.backend_server.domain.club.service.ClubService;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ApplicantResponseDto;
import com.kakaotech.team18.backend_server.global.dto.SuccessResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "동아리 API", description = "동아리 정보 조회 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clubs")
public class ClubController{

    private final ClubService clubService;

    @Operation(summary = "전체 동아리 목록 조회", description = "모집 상태와 함께 전체 동아리 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseEntity<ClubListResponseDto> getAllClubs() {
        ClubListResponseDto response = clubService.getAllClubs();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "특정 동아리 상세 정보 조회", description = "특정 동아리의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 동아리를 찾을 수 없음")
    })
    @GetMapping("/{clubId}")
    public ResponseEntity<ClubDetailResponseDto> getClub(
            @Parameter(description = "동아리의 고유 ID", required = true, example = "1") @PathVariable Long clubId
    ) {
        ClubDetailResponseDto response = clubService.getClubDetail(clubId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "특정 동아리 상세 정보 수정", description = "특정 동아리의 상세 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 동아리를 찾을 수 없음")
    })
    @PostMapping("/{clubId}")
    public ResponseEntity<SuccessResponseDto> updateClub(
            @Parameter(description = "동아리의 고유 ID", required = true, example = "1") @PathVariable Long clubId,
            @Valid @RequestBody ClubDetailRequestDto dto
    ) {
        SuccessResponseDto response = clubService.updateClubDetail(clubId,dto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "카테고리별 동아리 목록 조회", description = "특정 카테고리에 속한 동아리 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/search/category")
    public ResponseEntity<ClubListResponseDto> listClubsByCategory(
            @Parameter(description = "조회할 동아리 카테고리", required = true, example = "SPORTS") @RequestParam String category
    ){
        ClubListResponseDto response = clubService.getClubByCategory(category);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "동아리 대시보드 정보 조회", description = "운영진이 동아리 관리 페이지에서 필요한 정보(예: 지원자 현황)를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 동아리를 찾을 수 없음")
    })
    @GetMapping("/{clubId}/dashboard")
    public ResponseEntity<ClubDashBoardResponseDto> getClubDashboard(
            @Parameter(description = "동아리의 고유 ID", required = true, example = "1") @PathVariable Long clubId
    ) {
        ClubDashBoardResponseDto response = clubService.getClubDashBoard(clubId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "동아리 대쉬보드에서 지원자 목록 필터링 조회", description = "운영진이 동아리 관리 페이지에서 지원자 목록을 필터링을 통해 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
    })
    @GetMapping("/{clubId}/dashboard/applicants")
    public ResponseEntity<List<ApplicantResponseDto>> getClubApplicants(
            @PathVariable Long clubId,
            @RequestParam(required = false) Status status) {
        List<ApplicantResponseDto> response = clubService.getApplicantsByStatus(clubId, status);
        return ResponseEntity.ok(response);
    }
}
