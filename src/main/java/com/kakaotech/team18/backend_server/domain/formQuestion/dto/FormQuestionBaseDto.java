package com.kakaotech.team18.backend_server.domain.formQuestion.dto;

import com.kakaotech.team18.backend_server.domain.formQuestion.entity.FieldType;
import java.util.List;

public interface FormQuestionBaseDto {
    String question();
    FieldType fieldType();
    Boolean isRequired();
    Long displayOrder();
    List<String> optionList();
    List<TimeSlotOptionRequestDto> timeSlotOptions();
}