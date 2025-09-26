package com.kakaotech.team18.backend_server.domain.application.service;

import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationApplyRequestDto;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationApplyResponseDto;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationDetailResponseDto;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationStatusUpdateRequestDto;
import com.kakaotech.team18.backend_server.global.dto.SuccessResponseDto;
import jakarta.validation.Valid;

public interface ApplicationService {
    ApplicationDetailResponseDto getApplicationDetail(Long clubId, Long applicantId);

    SuccessResponseDto updateApplicationStatus(Long applicationId, ApplicationStatusUpdateRequestDto requestDto);

    ApplicationApplyResponseDto submitApplication(Long clubId, ApplicationApplyRequestDto request, boolean confirmOverwrite);
}
