package com.kakaotech.team18.backend_server.domain.applicationForm.dto;

import com.kakaotech.team18.backend_server.domain.applicationFormField.dto.ApplicationFormFieldResponseDto;
import lombok.Getter;

import java.util.List;

public record ApplicationFormResponseDto(
        String title,
        String description,
        List<ApplicationFormFieldResponseDto> questions
) {
    public static ApplicationFormResponseDto of(
            String title,
            String description,
            List<ApplicationFormFieldResponseDto> questions
    ) {
        return new ApplicationFormResponseDto(title, description, questions);
    }
}
