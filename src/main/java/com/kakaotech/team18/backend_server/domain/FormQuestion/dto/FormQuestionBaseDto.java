package com.kakaotech.team18.backend_server.domain.FormQuestion.dto;

import com.kakaotech.team18.backend_server.domain.FormQuestion.entity.FieldType;
import java.util.List;

public interface FormQuestionBaseDto {
    String question();
    FieldType fieldType();
    Boolean isRequired();
    Long displayOrder();
    List<String> optionList();
    List<TimeSlotOptionRequestDto> timeSlotOptions();
}