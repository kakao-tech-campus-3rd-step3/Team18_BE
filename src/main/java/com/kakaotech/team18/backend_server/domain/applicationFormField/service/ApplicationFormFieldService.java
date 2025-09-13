package com.kakaotech.team18.backend_server.domain.applicationFormField.service;

import com.kakaotech.team18.backend_server.domain.applicationFormField.entity.ApplicationFormField;

import java.util.List;

public interface ApplicationFormFieldService {
    List<ApplicationFormField> getApplicationFormFieldsById(Long applicationFormId);
}
