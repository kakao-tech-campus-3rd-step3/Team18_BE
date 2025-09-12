package com.kakaotech.team18.backend_server.domain.applicationForm.service;

import com.kakaotech.team18.backend_server.domain.applicationForm.dto.ApplicationFormResponseDto;
import com.kakaotech.team18.backend_server.domain.applicationForm.entity.ApplicationForm;
import com.kakaotech.team18.backend_server.domain.applicationForm.repository.ApplicationFormRepository;
import com.kakaotech.team18.backend_server.domain.applicationFormField.dto.ApplicationFormFieldResponseDto;
import com.kakaotech.team18.backend_server.domain.applicationFormField.repository.ApplicationFormFieldRepository;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ApplicationFormNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ApplicationFormServiceImpl implements ApplicationFormService {
    private final ApplicationFormFieldRepository applicationFormFieldRepository;
    private final ApplicationFormRepository applicationFormRepository;

    @Transactional(readOnly = true)
    public ApplicationFormResponseDto getQuestionForm(Long clubId){
        ApplicationForm applicationForm = applicationFormRepository.findByClubIdAndIsActiveTrue(clubId).orElseThrow(()->new ApplicationFormNotFoundException(clubId) );

        Long formId = applicationForm.getId();
        String title = applicationForm.getTitle();
        String description = applicationForm.getDescription();

        List<ApplicationFormFieldResponseDto> questions =
                applicationFormFieldRepository.findByApplicationFormIdOrderByDisplayOrderAsc(formId)
                        .stream()
                        .map(ApplicationFormFieldResponseDto::from)
                        .toList();

        return ApplicationFormResponseDto.of(title, description, questions);
    }
}
