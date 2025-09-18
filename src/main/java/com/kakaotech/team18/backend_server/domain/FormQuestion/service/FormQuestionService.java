package com.kakaotech.team18.backend_server.domain.FormQuestion.service;

import com.kakaotech.team18.backend_server.domain.FormQuestion.entity.FormQuestion;

import java.util.List;

public interface FormQuestionService {
    List<FormQuestion> getApplicationFormFieldsById(Long applicationFormId);
}
