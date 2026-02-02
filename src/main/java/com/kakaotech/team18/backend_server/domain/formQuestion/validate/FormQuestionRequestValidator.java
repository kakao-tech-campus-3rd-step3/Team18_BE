package com.kakaotech.team18.backend_server.domain.formQuestion.validate;

import static com.kakaotech.team18.backend_server.domain.formQuestion.entity.FieldType.CHECKBOX;
import static com.kakaotech.team18.backend_server.domain.formQuestion.entity.FieldType.RADIO;
import static com.kakaotech.team18.backend_server.domain.formQuestion.entity.FieldType.TIME_SLOT;

import com.kakaotech.team18.backend_server.domain.formQuestion.dto.FormQuestionBaseDto;
import com.kakaotech.team18.backend_server.domain.formQuestion.dto.TimeSlotOptionRequestDto;
import com.kakaotech.team18.backend_server.domain.formQuestion.entity.FieldType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import jakarta.validation.ConstraintViolation;

public class FormQuestionRequestValidator implements ConstraintValidator<ValidFormQuestionRequest, FormQuestionBaseDto> {

    private final Validator validator;

    public FormQuestionRequestValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            this.validator = factory.getValidator();
        }
    }

    @Override
    public boolean isValid(FormQuestionBaseDto dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return fail(context, "질문 정보는 필수입니다.");
        }

        FieldType type = dto.fieldType();

        if (type == TIME_SLOT) {
            if (dto.timeSlotOptions() == null || dto.timeSlotOptions().isEmpty()) {
                return fail(context, "TIME_SLOT 유형은 timeSlotOptions를 입력해야 합니다.");
            }
            // TIME_SLOT일 때만 내부 요소 검증 수행
            for (int i = 0; i < dto.timeSlotOptions().size(); i++) {
                TimeSlotOptionRequestDto option = dto.timeSlotOptions().get(i);
                Set<ConstraintViolation<TimeSlotOptionRequestDto>> violations = validator.validate(option);
                if (!violations.isEmpty()) {
                    // 첫 번째 위반 사항만 메시지로 반환
                    ConstraintViolation<TimeSlotOptionRequestDto> violation = violations.iterator().next();
                    return fail(context, "timeSlotOptions[" + i + "]." + violation.getPropertyPath() + ": " + violation.getMessage());
                }
            }
        }

        if (type == RADIO || type == CHECKBOX) {
            if (dto.optionList() == null || dto.optionList().isEmpty()) {
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
