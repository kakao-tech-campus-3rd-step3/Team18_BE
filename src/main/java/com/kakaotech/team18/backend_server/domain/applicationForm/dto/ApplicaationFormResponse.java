package com.kakaotech.team18.backend_server.domain.applicationForm.dto;

import com.kakaotech.team18.backend_server.domain.applicationFormField.dto.ApplicationFormFieldResponseDto;

import java.util.List;


public class ApplicaationFormResponse {

    String title;
    String description;
    List<ApplicationFormFieldResponseDto> questions;

    public ApplicaationFormResponse(
            String title,
            String description,
            List<ApplicationFormFieldResponseDto> questions
    ) {
        this.title = title;
        this.description = description;
        this.questions = questions;
    }
}
