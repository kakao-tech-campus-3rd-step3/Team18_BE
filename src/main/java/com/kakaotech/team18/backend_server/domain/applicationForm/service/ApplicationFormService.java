package com.kakaotech.team18.backend_server.domain.applicationForm.service;

import com.kakaotech.team18.backend_server.domain.applicationForm.dto.ApplicationFormResponse;

public interface ApplicationFormService {
    ApplicationFormResponse getQuestionForm(Long clubId);
}
