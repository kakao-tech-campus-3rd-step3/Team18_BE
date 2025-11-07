package com.kakaotech.team18.backend_server.domain.formQuestion.service;

import com.kakaotech.team18.backend_server.domain.formQuestion.entity.FormQuestion;

import java.util.List;

public interface FormQuestionService {
    List<FormQuestion> getApplicationFormFieldsById(Long applicationFormId);
}
