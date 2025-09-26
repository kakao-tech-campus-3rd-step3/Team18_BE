package com.kakaotech.team18.backend_server.domain.application.controller;

import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationDetailResponseDto;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationStatusUpdateRequestDto;
import com.kakaotech.team18.backend_server.domain.application.service.ApplicationService;
import com.kakaotech.team18.backend_server.global.dto.SuccessResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @GetMapping("/api/clubs/{clubId}/applicants/{applicantId}/application")
    public ResponseEntity<ApplicationDetailResponseDto> getApplicationDetail(
            @PathVariable("clubId") Long clubId,
            @PathVariable("applicantId") Long applicantId
    ) {
        ApplicationDetailResponseDto applicationDetail = applicationService.getApplicationDetail(clubId, applicantId);
        return ResponseEntity.ok(applicationDetail);
    }

    @PatchMapping("/api/applications/{applicationId}")
    public ResponseEntity<SuccessResponseDto> updateApplicationStatus(
            @PathVariable("applicationId") Long applicationId,
            @Valid @RequestBody ApplicationStatusUpdateRequestDto requestDto
    ) {
        SuccessResponseDto responseDto = applicationService.updateApplicationStatus(applicationId, requestDto);
        return ResponseEntity.ok(responseDto);
    }
}
