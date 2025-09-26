package com.kakaotech.team18.backend_server.domain.clubApplyForm.service;

import com.kakaotech.team18.backend_server.domain.FormQuestion.dto.FormQuestionBaseDto;
import com.kakaotech.team18.backend_server.domain.FormQuestion.dto.FormQuestionResponseDto;
import com.kakaotech.team18.backend_server.domain.FormQuestion.dto.FormQuestionUpdateDto;
import com.kakaotech.team18.backend_server.domain.FormQuestion.dto.TimeSlotOptionRequestDto;
import com.kakaotech.team18.backend_server.domain.FormQuestion.entity.FieldType;
import com.kakaotech.team18.backend_server.domain.FormQuestion.entity.FormQuestion;
import com.kakaotech.team18.backend_server.domain.FormQuestion.entity.TimeSlotOption;
import com.kakaotech.team18.backend_server.domain.FormQuestion.repository.FormQuestionRepository;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.dto.ClubApplyFormRequestDto;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.dto.ClubApplyFormResponseDto;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.dto.ClubApplyFormUpdateDto;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.repository.ClubApplyFormRepository;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ApplicationFormNotFoundException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ClubApplyFormNotFoundException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ClubApplyFormNotFoundException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ClubNotFoundException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
        Club findClub = findClub(clubId);
        ClubApplyForm clubApplyForm = createClubApplyForm(request, findClub);
        ClubApplyForm savedClubApplyForm = clubApplyFormRepository.save(clubApplyForm);
        log.info("Saved ClubApplyFormId: {}", savedClubApplyForm.getId());

        request.formQuestions().forEach(formQuestionRequestDto -> {
            FormQuestion formQuestion = createFormQuestion(formQuestionRequestDto, savedClubApplyForm);
            formQuestionRepository.save(formQuestion);
            log.info("Saved FormQuestionId: {}", formQuestion.getId());
        });
    }

    @Override
    @Transactional
    public void updateClubApplyForm(Long clubId, ClubApplyFormUpdateDto request) {
        Club club = findClub(clubId);
        ClubApplyForm findClubApplyForm = clubApplyFormRepository.findByClubId(club.getId())
                .orElseThrow(() -> {
                    log.warn("ClubApplyForm not found for clubId: {}", clubId);
                    return new ClubApplyFormNotFoundException("clubId = " + club.getId());
                });

        findClubApplyForm.update(request.title(), request.description());
        //기존 FormQuestion 찾아서 Map에 등록
        Map<Long, FormQuestion> existingMap = formQuestionRepository.findByClubApplyForm(
                        findClubApplyForm).stream()
                .collect(Collectors.toMap(FormQuestion::getId, fq -> fq));

        Set<Long> incomingIds = new HashSet<>(); // 삭제할 FormQuestion을 찾기 위함
        syncFormQuestions(request, existingMap, incomingIds, findClubApplyForm);
        removeLegacyQuestions(existingMap, incomingIds);
    }

    private void syncFormQuestions(
            ClubApplyFormUpdateDto request,
            Map<Long, FormQuestion> existingMap,
            Set<Long> incomingIds,
            ClubApplyForm findClubApplyForm
    ) {
        for (FormQuestionUpdateDto dto : request.formQuestions()) {
            if (dto.questionId() != null && existingMap.containsKey(dto.questionId())) {
                existingMap.get(dto.questionId()).updateFrom(dto);
                log.info("Updated FormQuestionId: {}", dto.questionId());
                incomingIds.add(dto.questionId());
            } else {
                FormQuestion newQuestion = createFormQuestion(dto, findClubApplyForm);
                FormQuestion savedFormQuestion = formQuestionRepository.save(newQuestion);
                log.info("Created new FormQuestionId: {}", savedFormQuestion.getId());
            }
        }
    }

    private void removeLegacyQuestions(Map<Long, FormQuestion> existingMap, Set<Long> incomingIds) {
        for (Long existingId : existingMap.keySet()) {
            if (!incomingIds.contains(existingId)) {
                formQuestionRepository.deleteById(existingId);
                log.info("Deleted FormQuestionId: {}", existingId);
            }
        }
    }

    private Club findClub(Long clubId) {
        return clubRepository.findById(clubId).orElseThrow(() -> {
            log.warn("Club not found for clubId: {}", clubId);
            return new ClubNotFoundException("clubId = " + clubId);
        });
    }

    private ClubApplyForm createClubApplyForm(ClubApplyFormRequestDto request, Club findClub) {
        return ClubApplyForm.builder()
                .club(findClub)
                .title(request.title())
                .description(request.description())
                .build();
    }

    private FormQuestion createFormQuestion(FormQuestionBaseDto dto, ClubApplyForm savedForm) {
        FormQuestion.FormQuestionBuilder builder = FormQuestion.builder()
                .clubApplyForm(savedForm)
                .question(dto.question())
                .fieldType(dto.fieldType())
                .isRequired(dto.isRequired())
                .displayOrder(dto.displayOrder());

        if (isTimeSlot(dto)) {
            List<TimeSlotOptionRequestDto> timeSlots = dto.timeSlotOptions();
            builder.timeSlotOptions(timeSlots != null
                    ? timeSlots.stream()
                    .map(tsoDto -> new TimeSlotOption(
                            LocalDate.parse(tsoDto.date()),
                            new TimeSlotOption.TimeRange(
                                    tsoDto.availableTime().start(),
                                    tsoDto.availableTime().end()
                            )
                    ))
                    .toList()
                    : List.of());
        } else {
            builder.options(dto.optionList());  // RADIO, CHECKBOX 등에서 사용
        }
        return builder.build();
    }

    private boolean isTimeSlot(FormQuestionBaseDto dto) {
        return dto.fieldType() == FieldType.TIME_SLOT;
    }
}
