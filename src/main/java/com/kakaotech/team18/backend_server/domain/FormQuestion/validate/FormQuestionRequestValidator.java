package com.kakaotech.team18.backend_server.domain.FormQuestion.validate;

import static com.kakaotech.team18.backend_server.domain.FormQuestion.entity.FieldType.CHECKBOX;
import static com.kakaotech.team18.backend_server.domain.FormQuestion.entity.FieldType.RADIO;
import static com.kakaotech.team18.backend_server.domain.FormQuestion.entity.FieldType.TIME_SLOT;

import com.kakaotech.team18.backend_server.domain.FormQuestion.dto.FormQuestionRequestDto;
import com.kakaotech.team18.backend_server.domain.FormQuestion.entity.FieldType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FormQuestionRequestValidator implements ConstraintValidator<ValidFormQuestionRequest, FormQuestionRequestDto> {

    @Override
    public boolean isValid(FormQuestionRequestDto dto, ConstraintValidatorContext context) {

        FieldType type = dto.fieldType();

        if (type == TIME_SLOT) {
            if (dto.timeSlotOptions() == null || dto.timeSlotOptions().isEmpty()) {
                return fail(context, "TIME_SLOT 유형은 timeSlotOptions를 입력해야 합니다.");
            }
        }

        if (type == RADIO || type == CHECKBOX) {
            if (dto.options() == null || dto.options().isEmpty()) {
                return fail(context, "선택형 질문은 options를 입력해야 합니다.");
            }
        }

        return true;
    }

    private boolean fail(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
        return false;
    }
}