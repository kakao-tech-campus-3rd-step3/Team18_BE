package com.kakaotech.team18.backend_server.domain.application.service;

import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationApplyRequestDto;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationApplyResponseDto;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationApprovedRequestDto;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationDetailResponseDto;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationStatusUpdateRequestDto;
import com.kakaotech.team18.backend_server.domain.application.entity.Stage;
import com.kakaotech.team18.backend_server.global.dto.SuccessResponseDto;

public interface ApplicationService {
    ApplicationDetailResponseDto getApplicationDetail(Long clubId, Long applicantId);

    SuccessResponseDto updateApplicationStatus(Long applicationId, ApplicationStatusUpdateRequestDto requestDto);

    ApplicationApplyResponseDto submitApplication(Long clubId, ApplicationApplyRequestDto request, boolean confirmOverwrite);

    SuccessResponseDto sendPassFailMessage(Long clubId, ApplicationApprovedRequestDto requestDto);

    SuccessResponseDto sendPassFailMessage(long clubId, List<AnswerEmailLine> emailLines);

    SuccessResponseDto sendPassFailMessage(Long clubId, ApplicationApprovedRequestDto requestDto, Stage stage);
}
