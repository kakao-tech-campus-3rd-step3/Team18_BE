package com.kakaotech.team18.backend_server.domain.applicationForm.service;

import com.kakaotech.team18.backend_server.domain.applicationForm.dto.ApplicationFormResponseDto;

public interface ApplicationFormService {
    ApplicationFormResponseDto getQuestionForm(Long clubId);
}
