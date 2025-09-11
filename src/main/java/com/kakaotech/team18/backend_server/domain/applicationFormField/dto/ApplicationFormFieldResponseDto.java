package com.kakaotech.team18.backend_server.domain.applicationFormField.dto;

import com.kakaotech.team18.backend_server.domain.applicationFormField.entity.ApplicationFormField;
import com.kakaotech.team18.backend_server.domain.applicationFormField.entity.FieldType;
import java.util.List;

public record ApplicationFormFieldResponseDto(
        int questionNum,
        FieldType questionType,
        String question,
        boolean required,
        List<String> answerList
) {
    public static ApplicationFormFieldResponseDto from(ApplicationFormField field) {
        return new ApplicationFormFieldResponseDto(
                field.getDisplayOrder().intValue(),
                field.getFieldType(),
                field.getQuestion(),
                field.isRequired(),
                field.getOptions()
        );
    }
}
