package com.kakaotech.team18.backend_server.domain.clubApplyForm.service;

import com.kakaotech.team18.backend_server.domain.FormQuestion.dto.FormQuestionRequestDto;
import com.kakaotech.team18.backend_server.domain.FormQuestion.dto.FormQuestionResponseDto;
import com.kakaotech.team18.backend_server.domain.FormQuestion.entity.FieldType;
import com.kakaotech.team18.backend_server.domain.FormQuestion.entity.FormQuestion;
import com.kakaotech.team18.backend_server.domain.FormQuestion.entity.TimeSlotOption;
import com.kakaotech.team18.backend_server.domain.FormQuestion.repository.FormQuestionRepository;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.dto.ClubApplyFormRequestDto;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.dto.ClubApplyFormResponseDto;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.repository.ClubApplyFormRepository;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ClubApplyFormNotFoundException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ClubNotFoundException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
public class ClubApplyFormServiceImpl implements ClubApplyFormService {
    private final FormQuestionRepository formQuestionRepository;
    private final ClubApplyFormRepository clubApplyFormRepository;
    private final ClubRepository clubRepository;

    @Transactional(readOnly = true)
    public ClubApplyFormResponseDto getQuestionForm(Long clubId){
        ClubApplyForm clubApplyForm = clubApplyFormRepository.findByClubIdAndIsActiveTrue(clubId)
                .orElseThrow(() -> {
                    log.warn("ClubApplyForm not found for clubId: {}", clubId);
                    return new ClubApplyFormNotFoundException("clubId = " + clubId);
                });

        Long formId = clubApplyForm.getId();
        String title = clubApplyForm.getTitle();
        String description = clubApplyForm.getDescription();

        List<FormQuestionResponseDto> questions =
                formQuestionRepository.findByClubApplyFormIdOrderByDisplayOrderAsc(formId)
                        .stream()
                        .map(FormQuestionResponseDto::from)
                        .toList();

        return ClubApplyFormResponseDto.of(title, description, questions);
    }

    @Override
    @Transactional
    public void createClubApplyForm(Long clubId, ClubApplyFormRequestDto request) {
        Club findClub = clubRepository.findById(clubId).orElseThrow(() -> {
            log.warn("Club not found for clubId: {}", clubId);
            return new ClubNotFoundException("clubId");
        });

        ClubApplyForm clubApplyForm = createClubApplyForm(request, findClub);
        ClubApplyForm savedClubApplyForm = clubApplyFormRepository.save(clubApplyForm);
        log.info("Saved ClubApplyFormId: {}", savedClubApplyForm.getId());

        request.formQuestions().forEach(formQuestionRequestDto -> {
            FormQuestion formQuestion = createFormQuestion(formQuestionRequestDto, savedClubApplyForm);
            formQuestionRepository.save(formQuestion);
            log.info("Saved FormQuestionId: {}", formQuestion.getId());
        });
    }

    private static ClubApplyForm createClubApplyForm(ClubApplyFormRequestDto request, Club findClub) {
        return ClubApplyForm.builder()
                .club(findClub)
                .title(request.title())
                .description(request.description())
                .build();
    }

    private static FormQuestion createFormQuestion(FormQuestionRequestDto dto, ClubApplyForm savedForm) {
        FormQuestion.FormQuestionBuilder builder = FormQuestion.builder()
                .clubApplyForm(savedForm)
                .question(dto.question())
                .fieldType(dto.fieldType())
                .isRequired(dto.isRequired())
                .displayOrder(dto.displayOrder());

        if (isTimeSlot(dto)) {
            builder.timeSlotOptions(dto.timeSlotOptions().stream()
                    .map(tsoDto -> new TimeSlotOption(
                            LocalDate.parse(tsoDto.date()),
                            new TimeSlotOption.TimeRange(
                                    tsoDto.availableTime().start(),
                                    tsoDto.availableTime().end()
                            )
                    ))
                    .toList());
        } else {
            builder.options(dto.options());  // RADIO, CHECKBOX 등에서 사용
        }
        return builder.build();
    }

    private static boolean isTimeSlot(FormQuestionRequestDto dto) {
        return dto.fieldType() == FieldType.TIME_SLOT;
    }
}
