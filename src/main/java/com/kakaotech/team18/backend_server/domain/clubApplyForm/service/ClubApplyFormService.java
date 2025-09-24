package com.kakaotech.team18.backend_server.domain.clubApplyForm.service;

import com.kakaotech.team18.backend_server.domain.clubApplyForm.dto.ClubApplyFormRequestDto;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.dto.ClubApplyFormResponseDto;

public interface ClubApplyFormService {
    ClubApplyFormResponseDto getQuestionForm(Long clubId);

    void createClubApplyForm(Long clubId, ClubApplyFormRequestDto request);
}
