package com.kakaotech.team18.backend_server.domain.applicationFormField.service;

import com.kakaotech.team18.backend_server.domain.applicationFormField.repository.ApplicationFormFieldRepository;
import org.springframework.stereotype.Service;

@Service
public class ApplicationFormFieldServiceImpl implements ApplicationFormFieldService {
        private final ApplicationFormFieldRepository applicationFormFieldRepository;

        public ApplicationFormFieldServiceImpl(
                ApplicationFormFieldRepository applicationFormFieldRepository
        ) {
                this.applicationFormFieldRepository = applicationFormFieldRepository;
        }
}
