package com.kakaotech.team18.backend_server.domain.clubApplyForm.service;

import com.kakaotech.team18.backend_server.domain.clubApplyForm.dto.ClubApplyFormResponseDto;

public interface ApplicationFormService {
    ClubApplyFormResponseDto getQuestionForm(Long clubId);
}
