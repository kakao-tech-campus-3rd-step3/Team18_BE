package com.kakaotech.team18.backend_server.domain.application.controller;

import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationApplyRequestDto;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationApplyResponseDto;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationDetailResponseDto;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationStatusUpdateRequestDto;
import com.kakaotech.team18.backend_server.domain.application.service.ApplicationService;
import com.kakaotech.team18.backend_server.global.dto.SuccessResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "지원서 API", description = "지원서 조회 및 상태 변경 관련 API")
@RestController
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @Operation(summary = "지원서 상세 조회", description = "동아리 운영진이 특정 지원자의 지원서를 상세 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 지원서를 찾을 수 없음")
    })
    @GetMapping("/api/clubs/{clubId}/applicants/{applicantId}/application")
    public ResponseEntity<ApplicationDetailResponseDto> getApplicationDetail(
            @Parameter(description = "동아리의 고유 ID", required = true, example = "1") @PathVariable("clubId") Long clubId,
            @Parameter(description = "지원자의 고유 ID (User ID)", required = true, example = "12") @PathVariable("applicantId") Long applicantId
    ) {
        ApplicationDetailResponseDto applicationDetail = applicationService.getApplicationDetail(clubId, applicantId);
        return ResponseEntity.ok(applicationDetail);
    }

    @Operation(summary = "지원서 상태 변경", description = "동아리 운영진이 지원서의 상태(예: 보류, 합격, 불합격)를 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상태 변경 성공"),
            @ApiResponse(responseCode = "404", description = "상태를 변경할 지원서를 찾을 수 없음")
    })
    @PatchMapping("/api/applications/{applicationId}")
    public ResponseEntity<SuccessResponseDto> updateApplicationStatus(
            @Parameter(description = "상태를 변경할 지원서의 고유 ID", required = true, example = "100") @PathVariable("applicationId") Long applicationId,
            @Valid @RequestBody ApplicationStatusUpdateRequestDto requestDto
    ) {
        SuccessResponseDto responseDto = applicationService.updateApplicationStatus(applicationId, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/api/clubs/{clubId}/apply-submit")
    public ResponseEntity<ApplicationApplyResponseDto> submitApplication(
            @PathVariable("clubId") Long clubId,
            @Valid @RequestBody ApplicationApplyRequestDto request,
            @RequestParam(value = "overwrite", defaultValue = "false" ) boolean requiresConfirmation
    ){
        ApplicationApplyResponseDto response = applicationService.submitApplication(
                clubId,
                request,
                requiresConfirmation
        );

        if(response.requiresConfirmation()){
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);//기존 응답이 있어서 확인
        } else {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);//기존 응답이 없어서 바로 제출
        }
    }
}
