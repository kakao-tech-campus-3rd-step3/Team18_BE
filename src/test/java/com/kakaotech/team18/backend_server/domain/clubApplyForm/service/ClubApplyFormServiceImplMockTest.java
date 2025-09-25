package com.kakaotech.team18.backend_server.domain.clubApplyForm.service;


import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.times;
import static org.mockito.Mockito.mock;

import com.kakaotech.team18.backend_server.domain.FormQuestion.dto.FormQuestionRequestDto;
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
import com.kakaotech.team18.backend_server.global.exception.exceptions.ClubNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ClubApplyFormServiceImplMockTest {

    @Mock
    private FormQuestionRepository formQuestionRepository;
    @Mock
    private ClubApplyFormRepository clubApplyFormRepository;
    @Mock
    private ClubRepository clubRepository;
    @InjectMocks
    private ClubApplyFormServiceImpl clubApplyFormService;


    @DisplayName("지원폼 조회 - 성공")
    @Test
    void getQuestionForm() {
        //given
        Long clubId = 1L;
        Club club = mock(Club.class);
        ClubApplyForm clubApplyForm = createClubApplyForm(club);
        ReflectionTestUtils.setField(clubApplyForm, "id", 1L);
        FormQuestion formQuestion = createFormQuestion(clubApplyForm);

        given(clubApplyFormRepository.findByClubIdAndIsActiveTrue(clubId)).willReturn(Optional.of(clubApplyForm));
        given(formQuestionRepository.findByClubApplyFormIdOrderByDisplayOrderAsc(clubApplyForm.getId())).willReturn(List.of(formQuestion));

        ClubApplyFormResponseDto expected = ClubApplyFormResponseDto.of(
                clubApplyForm.getTitle(),
                clubApplyForm.getDescription(),
                List.of(formQuestion).stream().map(
                                fq -> new FormQuestionResponseDto(
                                        fq.getId(),
                                        fq.getDisplayOrder(),
                                        fq.getFieldType(),
                                        fq.getQuestion(),
                                        fq.isRequired(),
                                        fq.getOptions(),
                                        fq.getTimeSlotOptions()
                                ))
                        .toList());

        //when
        ClubApplyFormResponseDto actual = clubApplyFormService.getQuestionForm(clubId);

        //then
        Assertions.assertThat(actual).isEqualTo(expected);
        then(clubApplyFormRepository).should(times(1)).findByClubIdAndIsActiveTrue(clubId);
        then(formQuestionRepository).should(times(1)).findByClubApplyFormIdOrderByDisplayOrderAsc(clubApplyForm.getId());
    }

    @DisplayName("지원폼 조회 - 실패 (지원폼이 존재하지 않는 경우)")
    @Test
    void getQuestionForm_ClubNotFound() {
        //given
        Long clubId = 1L;
        given(clubApplyFormRepository.findByClubIdAndIsActiveTrue(clubId)).willReturn(Optional.empty());

        //when, then
        Assertions.assertThatThrownBy(() -> clubApplyFormService.getQuestionForm(clubId))
                .isInstanceOf(ApplicationFormNotFoundException.class)
                .hasMessageContaining("지원폼이 존재하지 않습니다");

        then(clubApplyFormRepository).should(times(1)).findByClubIdAndIsActiveTrue(clubId);
        then(formQuestionRepository).should(never()).findByClubApplyFormIdOrderByDisplayOrderAsc(anyLong());
    }

    @DisplayName("지원폼 저장 - 성공")
    @Test
    void createClubApplyForm() {
        //given
        Long clubId = 1L;
        Club club = Club.builder().name("Test Club").build();

        FormQuestionRequestDto question1 = new FormQuestionRequestDto("질문 1", FieldType.TEXT, true, 1L, null, null);
        FormQuestionRequestDto question2 = new FormQuestionRequestDto("질문 2", FieldType.RADIO,
                false, 2L, List.of("옵션 1", "옵션 2"),
                List.of(new TimeSlotOptionRequestDto("2025-09-24", new TimeSlotOptionRequestDto.TimeRange("10:00", "21:00")))
        );
        ClubApplyFormRequestDto requestDto = new ClubApplyFormRequestDto("테스트 지원서", "테스트 설명", List.of(question1, question2));

        ClubApplyForm clubApplyForm = ClubApplyForm.builder()
                .club(club)
                .title(requestDto.title())
                .description(requestDto.description())
                .build();

        given(clubRepository.findById(clubId)).willReturn(Optional.of(club));
        given(clubApplyFormRepository.save(any(ClubApplyForm.class))).willReturn(clubApplyForm);
        given(formQuestionRepository.save(any(FormQuestion.class))).willAnswer(invocation -> {
            FormQuestion savedQuestion = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedQuestion, "id", 1L); // Simulate ID generation
            return savedQuestion;
        });

        //when
        clubApplyFormService.createClubApplyForm(clubId, requestDto);

        //then
        then(clubRepository).should(times(1)).findById(clubId);
        then(clubApplyFormRepository).should(times(1)).save(any(ClubApplyForm.class));
        then(formQuestionRepository).should(times(requestDto.formQuestions().size())).save(any(FormQuestion.class));
    }

    @DisplayName("지원폼 저장 - 실패 (동아리가 존재하지 않음)")
    @Test
    void createClubApplyForm_ClubNotFound() {
        //given
        Long clubId = 1L;
        FormQuestionRequestDto question1 = new FormQuestionRequestDto("질문 1", FieldType.TEXT, true, 1L, null, null);
        ClubApplyFormRequestDto requestDto = new ClubApplyFormRequestDto("테스트 지원서", "테스트 설명", List.of(question1));

        given(clubRepository.findById(clubId)).willReturn(Optional.empty());

        //when, then
        Assertions.assertThatThrownBy(() -> clubApplyFormService.createClubApplyForm(clubId, requestDto))
                .isInstanceOf(ClubNotFoundException.class)
                .hasMessageContaining("해당 동아리가 존재하지 않습니다.");

        then(clubRepository).should(times(1)).findById(clubId);
        then(clubApplyFormRepository).should(never()).save(any(ClubApplyForm.class));
        then(formQuestionRepository).should(never()).save(any(FormQuestion.class));
    }

    @DisplayName("지원폼 수정 - 성공")
    @Test
    void updateClubApplyForm() {
        //given
        Long clubId = 1L;
        Club club = mock(Club.class);
        ReflectionTestUtils.setField(club, "id", 1L);
        ClubApplyForm clubApplyForm = createClubApplyForm(club);
        ReflectionTestUtils.setField(clubApplyForm, "id", 1L);
        FormQuestion formQuestion1 = createFormQuestion(clubApplyForm);
        ReflectionTestUtils.setField(formQuestion1, "id", 1L);
        FormQuestion formQuestion2 = createFormQuestion(clubApplyForm);
        ReflectionTestUtils.setField(formQuestion2, "id", 2L);

        FormQuestion formQuestion3 = createFormQuestion(clubApplyForm);
        ReflectionTestUtils.setField(formQuestion3, "id", 3L);

        given(club.getId()).willReturn(1L);
        given(clubRepository.findById(clubId)).willReturn(Optional.of(club));
        given(clubApplyFormRepository.findByClubId(clubId)).willReturn(Optional.of(clubApplyForm));
        given(formQuestionRepository.findByClubApplyForm(clubApplyForm)).willReturn(List.of(formQuestion1, formQuestion2));

        // Update: question1 modified, question2 deleted, new question3 added
        ClubApplyFormUpdateDto requestDto = new ClubApplyFormUpdateDto(
                "수정된 지원서 제목",
                "수정된 지원서 설명",
                List.of(
                        new com.kakaotech.team18.backend_server.domain.FormQuestion.dto.FormQuestionUpdateDto(
                                1L,
                                "수정된 질문 1",
                                FieldType.TEXT,
                                false,
                                1L,
                                null,
                                null
                        ),
                        new com.kakaotech.team18.backend_server.domain.FormQuestion.dto.FormQuestionUpdateDto(
                                null,
                                "새로운 질문 3",
                                FieldType.CHECKBOX,
                                true,
                                3L,
                                List.of("옵션 A", "옵션 B"),
                                null
                        )
                )
        );

        //when
        clubApplyFormService.updateClubApplyForm(clubId, requestDto);

        //then
        then(clubRepository).should(times(1)).findById(clubId);
        then(clubApplyFormRepository).should(times(1)).findByClubId(clubId);
        then(formQuestionRepository).should(times(1)).findByClubApplyForm(clubApplyForm);
        then(formQuestionRepository).should(times(1)).save(any(FormQuestion.class)); // For the new question
        then(formQuestionRepository).should(times(1)).deleteById(2L); // For the deleted question
    }

    @DisplayName("지원폼 수정 - 실패 (지원폼이 존재하지 않음)")
    @Test
    void updateClubApplyForm_ClubApplyFormNotFound() {
        //given
        Long clubId = 1L;
        Club club = mock(Club.class);
        ReflectionTestUtils.setField(club, "id", 1L);
        FormQuestionUpdateDto question1 = new FormQuestionUpdateDto(1L, "질문 1", FieldType.TEXT, true, 1L, null, null);
        ClubApplyFormUpdateDto requestDto = new ClubApplyFormUpdateDto("테스트 지원서", "테스트 설명", List.of(question1));

        given(club.getId()).willReturn(1L);
        given(clubRepository.findById(clubId)).willReturn(Optional.of(club));
        given(clubApplyFormRepository.findByClubId(clubId)).willReturn(Optional.empty());

        //when, then
        Assertions.assertThatThrownBy(() -> clubApplyFormService.updateClubApplyForm(clubId, requestDto))
                .isInstanceOf(ClubApplyFormNotFoundException.class)
                .hasMessageContaining("지원폼이 존재하지 않습니다");

        then(clubRepository).should(times(1)).findById(clubId);
        then(clubApplyFormRepository).should(times(1)).findByClubId(clubId);
        then(clubApplyFormRepository).should(never()).save(any(ClubApplyForm.class));
        then(formQuestionRepository).should(never()).save(any(FormQuestion.class));    }


    private ClubApplyForm createClubApplyForm(Club findClub) {
        return ClubApplyForm.builder()
                .club(findClub)
                .title("먹짱 동아리 지원서")
                .description("전남대학교 먹짱 동아리 입니다.")
                .build();
    }

    private FormQuestion createFormQuestion(ClubApplyForm savedClubApplyForm) {
        return FormQuestion.builder()
                .clubApplyForm(savedClubApplyForm)
                .question("가장 좋아하는 음식은 무엇인가요?")
                .fieldType(FieldType.RADIO)
                .isRequired(true)
                .displayOrder(1L)
                .options(List.of("치킨", "피자", "햄버거"))
                .timeSlotOptions(List.of(new TimeSlotOption(LocalDate.of(2024, 10, 26),
                        new TimeSlotOption.TimeRange("10:00", "12:00")
                )))
                .build();
    }
}