package com.kakaotech.team18.backend_server.domain.clubMember.controller;

import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberResponseDto;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberRoleUpdateResponseDto;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberRoleUpdateRequestDto;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberSaveRequestDto;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberUpdateRequestDto;
import com.kakaotech.team18.backend_server.domain.clubMember.service.ClubMemberService;
import com.kakaotech.team18.backend_server.global.dto.SuccessResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    @Operation(summary = "동아리원 일괄 등록 (엑셀 업로드)", description = "엑셀 파일을 업로드하여 동아리원을 일괄 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "일괄 등록 성공"),
            @ApiResponse(responseCode = "400", description = "엑셀 데이터 검증 실패 또는 파일 형식 오류"),
            @ApiResponse(responseCode = "413", description = "파일 크기 초과"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (회장/운영진만 가능)")
    })
    @PreAuthorize("@customSecurityService.isClubAdminOrExecutive(#clubId)")
    @PostMapping(value = "/{clubId}/members/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponseDto> uploadMembers(
            @Parameter(description = "동아리 ID", required = true, example = "1") @PathVariable Long clubId,
            @Parameter(description = "엑셀 파일 (.xlsx, .xls)", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        clubMemberService.registerMembersByExcel(clubId, file);
        return ResponseEntity.ok(new SuccessResponseDto(true));
    }

    @Operation(summary = "동아리원 정보 수정", description = "동아리원의 정보를 부분 수정합니다. (직책 수정 불가)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력값"),
            @ApiResponse(responseCode = "409", description = "중복 학번 (다른 멤버가 사용 중)"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (회장/운영진만 가능)"),
            @ApiResponse(responseCode = "404", description = "해당 프로필을 찾을 수 없음")
    })
    @PreAuthorize("@customSecurityService.isClubAdminOrExecutive(#clubId)")
    @PatchMapping("/{clubId}/members/{profileId}")
    public ResponseEntity<ClubMemberResponseDto> updateMember(
            @Parameter(description = "동아리 ID", required = true, example = "1") @PathVariable Long clubId,
            @Parameter(description = "프로필 ID", required = true, example = "501") @PathVariable Long profileId,
            @Valid @RequestBody ClubMemberUpdateRequestDto requestDto
    ) {
        ClubMemberResponseDto response = clubMemberService.updateMember(clubId, profileId, requestDto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "동아리원 직책 변경 (권한 부여)", description = "동아리원의 직책을 변경합니다. (회장 전용)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "직책 변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력값"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (회장만 가능)"),
            @ApiResponse(responseCode = "404", description = "해당 프로필을 찾을 수 없음")
    })
    @PreAuthorize("@customSecurityService.isClubAdminOrExecutive(#clubId)")
    @PatchMapping("/{clubId}/members/{profileId}/role")
    public ResponseEntity<ClubMemberRoleUpdateResponseDto> updateMemberRole(
            @Parameter(description = "동아리 ID", required = true, example = "1") @PathVariable Long clubId,
            @Parameter(description = "프로필 ID", required = true, example = "501") @PathVariable Long profileId,
            @Valid @RequestBody ClubMemberRoleUpdateRequestDto requestDto
    ) {
        ClubMemberRoleUpdateResponseDto response = clubMemberService.updateMemberRole(clubId, profileId, requestDto);
        return ResponseEntity.ok(response);
    }
}
