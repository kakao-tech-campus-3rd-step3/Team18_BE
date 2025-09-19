package com.kakaotech.team18.backend_server.domain.application.controller;

import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationApplyRequestDto;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationApplyResponseDto;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationDetailResponseDto;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationStatusUpdateRequestDto;
import com.kakaotech.team18.backend_server.domain.application.service.ApplicationService;
import com.kakaotech.team18.backend_server.global.dto.SuccessResponseDto;
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

    @PostMapping("/api/clubs/{clubId}/apply-submit")
    public ResponseEntity<?> submitApplication(
            @PathVariable("clubId") Long clubId,
            @Valid @RequestBody ApplicationApplyRequestDto request,
            @RequestParam(value = "overwrite", defaultValue = "false" ) boolean confirmOverwrite
    ){
        ApplicationApplyResponseDto response = applicationService.submitApplication(
                clubId,
                request,
                confirmOverwrite
        );

        if(response.requiresConfirmation()){
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);//기존 응답이 있어서 확인
        } else {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);//기존 응답이 없어서 바로 제출
        }
    }
}
