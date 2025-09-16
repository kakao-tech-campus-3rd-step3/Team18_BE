package com.kakaotech.team18.backend_server.domain.FormQuestion.dto;

import com.kakaotech.team18.backend_server.domain.FormQuestion.entity.FormQuestion;
import com.kakaotech.team18.backend_server.domain.FormQuestion.entity.FieldType;
import java.util.List;

public record FormQuestionResponseDto(
        Long questionNum,
        FieldType questionType,
        String question,
        boolean required,
        List<String> optionList
) {
    public static FormQuestionResponseDto from(FormQuestion field) {
        return new FormQuestionResponseDto(
                field.getDisplayOrder(),
                field.getFieldType(),
                field.getQuestion(),
                field.isRequired(),
                field.getOptions()
        );
    }
}
