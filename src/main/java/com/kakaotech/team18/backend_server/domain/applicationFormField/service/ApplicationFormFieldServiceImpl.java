package com.kakaotech.team18.backend_server.domain.applicationFormField.service;

import com.kakaotech.team18.backend_server.domain.applicationFormField.entity.ApplicationFormField;
import com.kakaotech.team18.backend_server.domain.applicationFormField.repository.ApplicationFormFieldRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ApplicationFormFieldServiceImpl implements ApplicationFormFieldService {
        private final ApplicationFormFieldRepository applicationFormFieldRepository;

        public ApplicationFormFieldServiceImpl(
                ApplicationFormFieldRepository applicationFormFieldRepository
        ) {
                this.applicationFormFieldRepository = applicationFormFieldRepository;
        }

        public List<ApplicationFormField> getApplicationFormFieldsById(Long applicationFormId) {
                return applicationFormFieldRepository.findByApplicationFormIdOrderByFieldOrderAsc(applicationFormId);
        }
}
