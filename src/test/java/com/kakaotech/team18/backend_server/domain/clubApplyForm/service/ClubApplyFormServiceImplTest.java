package com.kakaotech.team18.backend_server.domain.clubApplyForm.service;


import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.repository.ClubApplyFormRepository;
import com.kakaotech.team18.backend_server.domain.FormQuestion.entity.FormQuestion;
import com.kakaotech.team18.backend_server.domain.FormQuestion.entity.FieldType;
import com.kakaotech.team18.backend_server.domain.FormQuestion.repository.FormQuestionRepository;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ClubApplyFormNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClubApplyFormServiceImplTest {

    @Mock
    private ClubApplyFormRepository clubApplyFormRepository;

    @Mock
    private FormQuestionRepository formQuestionRepository;

    @InjectMocks
    private ApplicationFormServiceImpl applicationFormServiceImpl;

    @Mock
    private Club mockClub;

    @Mock
    private ClubApplyForm mockClubApplyForm;

    @Mock
    private Application mockApplication;

    private ClubApplyForm clubApplyForm;
    private List<FormQuestion> formFields;

    @BeforeEach
    void setUp() {
        clubApplyForm = new ClubApplyForm(100L, mockClub, "카카오 동아리 지원서", "함께 성장할 팀원을 찾습니다.", true);

        FormQuestion textQuestion = new FormQuestion(1L, mockClubApplyForm, "이름", FieldType.TEXT, true, 1L, null);
        FormQuestion radioQuestion = new FormQuestion(2L, mockClubApplyForm, "성별", FieldType.RADIO, true, 2L, List.of("남","여"));
        FormQuestion checkboxQuestion = new FormQuestion(3L, mockClubApplyForm,"면접가능 요일",  FieldType.CHECKBOX, true, 3L, List.of("월","화","수","목","금","토"));

        formFields = List.of(textQuestion, radioQuestion, checkboxQuestion);
    }

    @Nested
    @DisplayName("getQuestionForm")
    class getQuestionForm{

        @Nested
        @DisplayName("Club with active form")
        class ClubWithActiveForm{

            @Test
            @DisplayName("returns dto")
            void returnsCompleteForm (){

                //given
                Long clubId = 100L;

                when(clubApplyFormRepository.findByClubIdAndIsActiveTrue(clubId)).thenReturn(Optional.of(
                        clubApplyForm));
                when(formQuestionRepository.findByClubApplyFormIdOrderByDisplayOrderAsc(
                        clubApplyForm.getId())).thenReturn(formFields);

                //when
                var result = applicationFormServiceImpl.getQuestionForm(clubId);

                //then
                assertThat(result.title()).isEqualTo("카카오 동아리 지원서");
                assertThat(result.description()).isEqualTo("함께 성장할 팀원을 찾습니다.");
                assertThat(result.questions()).hasSize(3);

                var firstQuestion = result.questions().get(0);
                assertThat(firstQuestion.questionNum()).isEqualTo(1L);
                assertThat(firstQuestion.question()).isEqualTo("이름");

                var secondQuestion = result.questions().get(1);
                assertThat(secondQuestion.questionNum()).isEqualTo(2L);
                assertThat(secondQuestion.question()).isEqualTo("성별");
                assertThat(secondQuestion.optionList()).containsExactly("남", "여");

                var thirdQuestion = result.questions().get(2);
                assertThat(thirdQuestion.questionNum()).isEqualTo(3L);
                assertThat(thirdQuestion.question()).isEqualTo("면접가능 요일");
                assertThat(thirdQuestion.optionList()).containsExactly("월", "화", "수", "목", "금", "토");

                verify(clubApplyFormRepository, times(1)).findByClubIdAndIsActiveTrue(clubId);
                verify(formQuestionRepository, times(1)).findByClubApplyFormIdOrderByDisplayOrderAsc(
                        clubApplyForm.getId());
            }

            @Test
            @DisplayName("폼은 있으나 질문(필드)이 없으면 빈 리스트로 반환한다")
            void returnsEmptyListWhenNoFields() {
                // given
                Long clubId = 100L;

                when(clubApplyFormRepository.findByClubIdAndIsActiveTrue(clubId))
                        .thenReturn(Optional.of(clubApplyForm));
                when(formQuestionRepository
                        .findByClubApplyFormIdOrderByDisplayOrderAsc(clubApplyForm.getId()))
                        .thenReturn(List.of());

                // when
                var result = applicationFormServiceImpl.getQuestionForm(clubId);

                // then
                assertThat(result.title()).isEqualTo("카카오 동아리 지원서");
                assertThat(result.description()).isEqualTo("함께 성장할 팀원을 찾습니다.");
                assertThat(result.questions()).isEmpty();

                verify(clubApplyFormRepository).findByClubIdAndIsActiveTrue(clubId);
                verify(formQuestionRepository)
                        .findByClubApplyFormIdOrderByDisplayOrderAsc(clubApplyForm.getId());
            }
        }

        @Nested
        @DisplayName("Club without active form")
        class ClubWithoutActiveForm{

            @Test
            @DisplayName("throws ApplicationFormNotFoundException")
            void throwsException() {
                // given
                Long clubId = 999L;
                when(clubApplyFormRepository.findByClubIdAndIsActiveTrue(clubId))
                        .thenReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> applicationFormServiceImpl.getQuestionForm(clubId))
                        .isInstanceOf(ClubApplyFormNotFoundException.class);

                verify(clubApplyFormRepository).findByClubIdAndIsActiveTrue(clubId);
                verifyNoInteractions(formQuestionRepository);
            }
        }
    }
}
