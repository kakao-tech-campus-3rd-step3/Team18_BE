package com.kakaotech.team18.backend_server.domain.clubApplyForm.controller;

import com.kakaotech.team18.backend_server.domain.clubApplyForm.dto.ClubApplyFormResponseDto;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.service.ClubApplyFormService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "지원서 양식 API", description = "동아리 지원서 양식(질문 폼) 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/clubs/{clubId}")
public class ClubApplyFormController {

    private final ClubApplyFormService applicationFormService;

    @Operation(summary = "지원서 양식 조회", description = "특정 동아리의 지원서 양식(질문 및 선택지 목록)을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 동아리 또는 지원서 양식을 찾을 수 없음")
    })
    @GetMapping("/apply")
    public ResponseEntity<ClubApplyFormResponseDto> getClubApplyFormByClubId(
            @Parameter(description = "동아리의 고유 ID", required = true, example = "1") @PathVariable("clubId") Long clubId
    ) {
        ClubApplyFormResponseDto response = applicationFormService.getQuestionForm(clubId);
        return ResponseEntity.ok(response);
    }
}
