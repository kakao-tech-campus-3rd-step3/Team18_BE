package com.kakaotech.team18.backend_server.domain.clubApplyForm.dto;

import com.kakaotech.team18.backend_server.domain.FormQuestion.dto.FormQuestionResponseDto;

import java.util.List;

public record ClubApplyFormResponseDto(
        String title,
        String description,
        List<FormQuestionResponseDto> questions
) {
    public static ClubApplyFormResponseDto of(
            String title,
            String description,
            List<FormQuestionResponseDto> questions
    ) {
        return new ClubApplyFormResponseDto(title, description, questions);
    }
}
