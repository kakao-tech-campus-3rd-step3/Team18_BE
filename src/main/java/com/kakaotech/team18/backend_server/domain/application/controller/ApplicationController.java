package com.kakaotech.team18.backend_server.domain.application.controller;

import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationDetailResponseDto;
import com.kakaotech.team18.backend_server.domain.application.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
}
