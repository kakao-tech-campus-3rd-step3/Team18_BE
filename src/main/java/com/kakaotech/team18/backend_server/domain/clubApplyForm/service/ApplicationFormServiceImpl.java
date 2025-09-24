package com.kakaotech.team18.backend_server.domain.clubApplyForm.service;

import com.kakaotech.team18.backend_server.domain.clubApplyForm.dto.ClubApplyFormResponseDto;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.repository.ClubApplyFormRepository;
import com.kakaotech.team18.backend_server.domain.FormQuestion.dto.FormQuestionResponseDto;
import com.kakaotech.team18.backend_server.domain.FormQuestion.repository.FormQuestionRepository;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ClubApplyFormNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ApplicationFormServiceImpl implements ApplicationFormService {
    private final FormQuestionRepository formQuestionRepository;
    private final ClubApplyFormRepository clubApplyFormRepository;

    @Transactional(readOnly = true)
    public ClubApplyFormResponseDto getQuestionForm(Long clubId){
        ClubApplyForm clubApplyForm = clubApplyFormRepository.findByClubIdAndIsActiveTrue(clubId).orElseThrow(()->new ClubApplyFormNotFoundException("clubId:"+clubId) );

        Long formId = clubApplyForm.getId();
        String title = clubApplyForm.getTitle();
        String description = clubApplyForm.getDescription();

        List<FormQuestionResponseDto> questions =
                formQuestionRepository.findByApplicationFormIdOrderByDisplayOrderAsc(formId)
                        .stream()
                        .map(FormQuestionResponseDto::from)
                        .toList();

        return ClubApplyFormResponseDto.of(title, description, questions);
    }
}
