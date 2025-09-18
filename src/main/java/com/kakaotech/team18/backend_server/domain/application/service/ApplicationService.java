package com.kakaotech.team18.backend_server.domain.application.service;

import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationDetailResponseDto;

public interface ApplicationService {
    ApplicationDetailResponseDto getApplicationDetail(Long clubId, Long applicantId);
}
