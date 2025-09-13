package com.kakaotech.team18.backend_server.domain.applicationFormField.service;

import com.kakaotech.team18.backend_server.domain.applicationFormField.entity.ApplicationFormField;
import com.kakaotech.team18.backend_server.domain.applicationFormField.repository.ApplicationFormFieldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ApplicationFormFieldServiceImpl implements ApplicationFormFieldService {
        private final ApplicationFormFieldRepository applicationFormFieldRepository;

        @Override
        @Transactional(readOnly = true)
        public List<ApplicationFormField> getApplicationFormFieldsById(Long applicationFormId) {
                return applicationFormFieldRepository.findByApplicationFormIdOrderByDisplayOrderAsc(applicationFormId);
        }
}
