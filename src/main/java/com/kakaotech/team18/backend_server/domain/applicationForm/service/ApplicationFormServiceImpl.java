package com.kakaotech.team18.backend_server.domain.applicationForm.service;

import com.kakaotech.team18.backend_server.domain.applicationForm.repository.ApplicationFormRepository;
import com.kakaotech.team18.backend_server.domain.applicationFormField.repository.ApplicationFormFieldRepository;
import org.springframework.stereotype.Service;

@Service
public class ApplicationFormServiceImpl implements ApplicationFormService {
    private final ApplicationFormFieldRepository applicationFormFieldRepository;
    private final ApplicationFormRepository applicationFormRepository;

    public ApplicationFormServiceImpl(
            ApplicationFormFieldRepository applicationFormFieldRepository,
            ApplicationFormRepository applicationFormRepository
    ){
        this.applicationFormFieldRepository = applicationFormFieldRepository;
        this.applicationFormRepository = applicationFormRepository;
    }
}
