package com.kakaotech.team18.backend_server.domain.clubApplyForm.dto;

import com.kakaotech.team18.backend_server.domain.applicationFormField.dto.ApplicationFormFieldResponseDto;

import java.util.List;

public record ClubApplyFormResponseDto(
        String title,
        String description,
        List<ApplicationFormFieldResponseDto> questions
) {
    public static ClubApplyFormResponseDto of(
            String title,
            String description,
            List<ApplicationFormFieldResponseDto> questions
    ) {
        return new ClubApplyFormResponseDto(title, description, questions);
    }
}
