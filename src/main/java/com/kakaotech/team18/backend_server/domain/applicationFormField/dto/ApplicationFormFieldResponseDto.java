package com.kakaotech.team18.backend_server.domain.applicationFormField.dto;

import com.kakaotech.team18.backend_server.domain.applicationFormField.entity.ApplicationFormField;
import com.kakaotech.team18.backend_server.domain.applicationFormField.entity.FieldType;
import java.util.List;

public record ApplicationFormFieldResponseDto(
        Long questionNum,
        FieldType questionType,
        String question,
        boolean required,
        List<String> optionList
) {
    public static ApplicationFormFieldResponseDto from(ApplicationFormField field) {
        return new ApplicationFormFieldResponseDto(
                field.getDisplayOrder(),
                field.getFieldType(),
                field.getQuestion(),
                field.isRequired(),
                field.getOptions()
        );
    }
}
