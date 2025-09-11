package com.kakaotech.team18.backend_server.domain.applicationForm.service;

import com.kakaotech.team18.backend_server.domain.applicationForm.dto.ApplicationFormResponse;
import com.kakaotech.team18.backend_server.domain.applicationForm.entity.ApplicationForm;
import com.kakaotech.team18.backend_server.domain.applicationForm.repository.ApplicationFormRepository;
import com.kakaotech.team18.backend_server.domain.applicationFormField.dto.ApplicationFormFieldResponseDto;
import com.kakaotech.team18.backend_server.domain.applicationFormField.repository.ApplicationFormFieldRepository;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public ApplicationFormResponse getQuestionForm(Long clubId){
        ApplicationForm applicationForm = applicationFormRepository.findByClubIdAndIsActiveTrue(clubId).orElseThrow(IllegalArgumentException::new);

        Long formId = applicationForm.getId();
        String title = applicationForm.getTitle();
        String description = applicationForm.getDescription();

        List<ApplicationFormFieldResponseDto> questions =
                applicationFormFieldRepository.findByApplicationFormIdOrderByDisplayOrderAsc(formId)
                        .stream()
                        .map(ApplicationFormFieldResponseDto::from)
                        .toList();

        return ApplicationFormResponse.of(title, description, questions);
    }
}
