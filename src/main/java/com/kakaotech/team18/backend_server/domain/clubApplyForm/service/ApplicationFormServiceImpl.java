package com.kakaotech.team18.backend_server.domain.clubApplyForm.service;

import com.kakaotech.team18.backend_server.domain.clubApplyForm.dto.ClubApplyFormResponseDto;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.repository.ClubApplyFormRepository;
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
    private final ClubApplyFormRepository clubApplyFormRepository;

    @Transactional(readOnly = true)
    public ClubApplyFormResponseDto getQuestionForm(Long clubId){
        ClubApplyForm clubApplyForm = clubApplyFormRepository.findByClubIdAndIsActiveTrue(clubId).orElseThrow(()->new ApplicationFormNotFoundException(clubId) );

        Long formId = clubApplyForm.getId();
        String title = clubApplyForm.getTitle();
        String description = clubApplyForm.getDescription();

        List<ApplicationFormFieldResponseDto> questions =
                applicationFormFieldRepository.findByApplicationFormIdOrderByDisplayOrderAsc(formId)
                        .stream()
                        .map(ApplicationFormFieldResponseDto::from)
                        .toList();

        return ClubApplyFormResponseDto.of(title, description, questions);
    }
}
