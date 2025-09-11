package com.kakaotech.team18.backend_server.domain.applicationForm.dto;

import com.kakaotech.team18.backend_server.domain.applicationFormField.dto.ApplicationFormFieldResponseDto;
import java.util.List;


public class ApplicationFormResponse {

    String title;
    String description;
    List<ApplicationFormFieldResponseDto> questions;

    public ApplicationFormResponse(
            String title,
            String description,
            List<ApplicationFormFieldResponseDto> questions
    ) {
        this.title = title;
        this.description = description;
        this.questions = questions;
    }

    public static ApplicationFormResponse of(
            String title,
            String description,
            List<ApplicationFormFieldResponseDto> questions
    ) {
        return new ApplicationFormResponse(title, description, questions);
    }
}
