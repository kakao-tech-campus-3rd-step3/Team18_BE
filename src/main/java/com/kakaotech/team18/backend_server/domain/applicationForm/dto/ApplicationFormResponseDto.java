package com.kakaotech.team18.backend_server.domain.applicationForm.dto;

import com.kakaotech.team18.backend_server.domain.applicationFormField.dto.ApplicationFormFieldResponseDto;
import lombok.Getter;

import java.util.List;

@Getter
public class ApplicationFormResponseDto {

    String title;
    String description;
    List<ApplicationFormFieldResponseDto> questions;

    public ApplicationFormResponseDto(
            String title,
            String description,
            List<ApplicationFormFieldResponseDto> questions
    ) {
        this.title = title;
        this.description = description;
        this.questions = questions;
    }

    public static ApplicationFormResponseDto of(
            String title,
            String description,
            List<ApplicationFormFieldResponseDto> questions) {
        return new ApplicationFormResponseDto(title, description, questions);
    }
}
