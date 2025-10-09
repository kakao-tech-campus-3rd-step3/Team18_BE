package com.kakaotech.team18.backend_server.domain.application.service;

import com.kakaotech.team18.backend_server.domain.answer.entity.Answer;
import com.kakaotech.team18.backend_server.domain.answer.repository.AnswerRepository;
import com.kakaotech.team18.backend_server.domain.FormQuestion.entity.FieldType;
import com.kakaotech.team18.backend_server.domain.FormQuestion.entity.FormQuestion;
import com.kakaotech.team18.backend_server.domain.FormQuestion.repository.FormQuestionRepository;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationApplyRequestDto;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationApplyResponseDto;
import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.application.repository.ApplicationRepository;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.repository.ClubApplyFormRepository;
import com.kakaotech.team18.backend_server.domain.email.dto.AnswerEmailLine;
import com.kakaotech.team18.backend_server.domain.email.dto.ApplicationSubmittedEvent;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.domain.user.repository.UserRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceSubmitApplicationTest {

    @InjectMocks
    private ApplicationServiceImpl service;

    @Mock private ApplicationRepository applicationRepository;
    @Mock private AnswerRepository answerRepository;
    @Mock private ClubApplyFormRepository clubApplyFormRepository;
    @Mock private FormQuestionRepository formQuestionRepository;
    @Mock private UserRepository userRepository;
    @Mock private ApplicationEventPublisher publisher;

    private final User baseUser = User.builder()
            .studentId("20231234")
            .email("stud@example.com")
            .name("홍길동")
            .phoneNumber("010-0000-0000")
            .department("컴퓨터공학과")
            .build();

    private List<FormQuestion> sampleQuestions(ClubApplyForm form) {

        FormQuestion q1 = FormQuestion.builder()
            .clubApplyForm(form)
            .question("자기소개")
            .fieldType(FieldType.TEXT)
            .isRequired(true)
            .displayOrder(1L)
            .build();
        FormQuestion q2 = FormQuestion.builder()
            .clubApplyForm(form)
            .question("성별")
            .fieldType(FieldType.RADIO)
            .isRequired(true)
            .displayOrder(2L)
            .options(List.of("남", "여"))
            .build();
        FormQuestion q3 = FormQuestion.builder()
            .clubApplyForm(form)
            .question("관심사").fieldType(FieldType.CHECKBOX).isRequired(false).displayOrder(3L).options(List.of("A", "B", "C")).build();

        ReflectionTestUtils.setField(q1, "id", 101L);
        ReflectionTestUtils.setField(q2, "id", 102L);
        ReflectionTestUtils.setField(q3, "id", 103L);

        return List.of(q1, q2, q3);
    }

    @Nested
    @DisplayName("submitApplication - 신규 제출")
    class CreateFlow {

        @Test
        @DisplayName("기존 지원서가 없으면 저장 후 이벤트 발행")
        void create_thenPublishEvent() {
            // given
            Club club = mock(Club.class);
            ClubApplyForm form = mock(ClubApplyForm.class);
            when(form.getId()).thenReturn(11L);

            when(clubApplyFormRepository.findByClubIdAndIsActiveTrue(1L))
                    .thenReturn(Optional.of(form));

            when(userRepository.findByStudentId("20231234"))
                    .thenReturn(Optional.empty());
            when(userRepository.save(any(User.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            when(applicationRepository.findByStudentIdAndClubApplyForm(eq("20231234"), eq(form)))
                    .thenReturn(Optional.empty());

            when(formQuestionRepository.findByClubApplyFormIdOrderByDisplayOrderAsc(11L))
                    .thenReturn(sampleQuestions(form));

            when(applicationRepository.save(any(Application.class)))
                    .thenAnswer(inv -> {
                        Application app = inv.getArgument(0);
                        ReflectionTestUtils.setField(app, "id", 100L);
                        ReflectionTestUtils.setField(app, "lastModifiedAt", LocalDateTime.now());
                        return app;
                    });

            when(answerRepository.saveAll(anyList()))
                    .thenAnswer(inv -> inv.getArgument(0));

            ApplicationApplyRequestDto req = new ApplicationApplyRequestDto(
                     "stud@example.com", "홍길동","20231234","010-0000-0000", "컴공",
                    List.of(
                            new ApplicationApplyRequestDto.AnswerDto(101L, "q","안녕하세요"),
                            new ApplicationApplyRequestDto.AnswerDto(102L, "q","여"),
                            new ApplicationApplyRequestDto.AnswerDto(103L, "q","A,B")
                    )
            );

            // when
            ApplicationApplyResponseDto res = service.submitApplication(1L, req, false);

            // then
            assertThat(res).isNotNull();
            assertThat(res.studentId()).isEqualTo("20231234");
            assertThat(res.requiresConfirmation()).isFalse();

            verify(applicationRepository, times(1)).save(any(Application.class));
            verify(answerRepository, times(1)).saveAll(anyList());
            verify(answerRepository, never()).deleteByApplication(any());

            // 이벤트 발행 검증
            ArgumentCaptor<ApplicationSubmittedEvent> captor =
                    ArgumentCaptor.forClass(ApplicationSubmittedEvent.class);
            verify(publisher, times(1)).publishEvent(captor.capture());
            ApplicationSubmittedEvent event = captor.getValue();
            assertThat(event.applicationId()).isEqualTo(100L);
            assertThat(event.emailLines()).hasSize(3);
        }
    }

    @Nested
    @DisplayName("submitApplication - 기존 제출 존재")
    class UpdateFlow {

        @Test
        @DisplayName("덮어쓰기 거부(false) → 기존 값 반환, 갱신/이벤트 없음")
        void existing_noOverwrite() {
            // given
            ClubApplyForm form = mock(ClubApplyForm.class);
            when(clubApplyFormRepository.findByClubIdAndIsActiveTrue(1L))
                    .thenReturn(Optional.of(form));

            when(userRepository.findByStudentId("20231234"))
                    .thenReturn(Optional.of(baseUser));

            Application existing = Application.builder().user(baseUser).clubApplyForm(form).build();
            ReflectionTestUtils.setField(existing, "id", 200L);
            ReflectionTestUtils.setField(existing, "lastModifiedAt", LocalDateTime.now());

            when(applicationRepository.findByStudentIdAndClubApplyForm(eq("20231234"), eq(form)))
                    .thenReturn(Optional.of(existing));

            ApplicationApplyRequestDto req = new ApplicationApplyRequestDto(
                     "stud@example.com", "홍길동","20231234", "010-0000-0000", "컴공",
                    List.of(
                            new ApplicationApplyRequestDto.AnswerDto(101L, "q","수정본문"),
                            new ApplicationApplyRequestDto.AnswerDto(102L, "q","남"),
                            new ApplicationApplyRequestDto.AnswerDto(103L, "q","A")
                    )
            );

            // when
            ApplicationApplyResponseDto res = service.submitApplication(1L, req, false);

            // then
            assertThat(res).isNotNull();
            assertThat(res.studentId()).isEqualTo("20231234");

            verify(answerRepository, never()).deleteByApplication(any());
            verify(answerRepository, never()).saveAll(anyList());
            verify(publisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("덮어쓰기 허용(true) → 답변 재저장 + 이벤트 발행")
        void existing_overwrite() {
            // given
            ClubApplyForm form = mock(ClubApplyForm.class);
            when(form.getId()).thenReturn(11L);

            when(clubApplyFormRepository.findByClubIdAndIsActiveTrue(1L))
                    .thenReturn(Optional.of(form));
            when(userRepository.findByStudentId("20231234"))
                    .thenReturn(Optional.of(baseUser));

            Application existing = Application.builder().user(baseUser).clubApplyForm(form).build();
            ReflectionTestUtils.setField(existing, "id", 201L);
            ReflectionTestUtils.setField(existing, "lastModifiedAt", LocalDateTime.now());

            when(applicationRepository.findByStudentIdAndClubApplyForm(eq("20231234"), eq(form)))
                    .thenReturn(Optional.of(existing));

            when(formQuestionRepository.findByClubApplyFormIdOrderByDisplayOrderAsc(11L))
                    .thenReturn(sampleQuestions(form));

            when(answerRepository.saveAll(anyList()))
                    .thenAnswer(inv -> inv.getArgument(0));

            ApplicationApplyRequestDto req = new ApplicationApplyRequestDto(
                    "stud@example.com", "홍길동","20231234", "010-0000-0000", "컴공",
                    List.of(
                            new ApplicationApplyRequestDto.AnswerDto(101L, "q","수정본문"),
                            new ApplicationApplyRequestDto.AnswerDto(102L, "q","여"),
                            new ApplicationApplyRequestDto.AnswerDto(103L, "q","B")
                    )
            );

            // when
            ApplicationApplyResponseDto res = service.submitApplication(1L, req, true);

            // then
            assertThat(res).isNotNull();
            assertThat(res.studentId()).isEqualTo("20231234");

            verify(answerRepository, times(1)).deleteByApplication(eq(existing));
            verify(answerRepository, times(1)).saveAll(anyList());

            ArgumentCaptor<ApplicationSubmittedEvent> captor =
                    ArgumentCaptor.forClass(ApplicationSubmittedEvent.class);
            verify(publisher, times(1)).publishEvent(captor.capture());
            assertThat(captor.getValue().applicationId()).isEqualTo(201L);
        }
    }

    @Nested
    @DisplayName("saveApplicationAnswers 동작")
    class SaveAnswers {

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("체크박스(비필수) 미응답 → Answer.answer=null & 이메일 '(미입력)'")
        void checkbox_optional_blankSavedAsNull() {
            // given
            ClubApplyForm form = mock(ClubApplyForm.class);
            when(form.getId()).thenReturn(11L);

            Application app = Application.builder().user(baseUser).clubApplyForm(form).build();
            ReflectionTestUtils.setField(app, "id", 300L);

            when(formQuestionRepository.findByClubApplyFormIdOrderByDisplayOrderAsc(11L))
                    .thenReturn(sampleQuestions(form));

            when(answerRepository.saveAll(anyList()))
                    .thenAnswer(inv -> inv.getArgument(0));

            // TEXT 값, RADIO 값, CHECKBOX 공란
            List<ApplicationApplyRequestDto.AnswerDto> answers = List.of(
                    new ApplicationApplyRequestDto.AnswerDto(101L, "q","자소서"),
                    new ApplicationApplyRequestDto.AnswerDto(102L, "q","남"),
                    new ApplicationApplyRequestDto.AnswerDto(103L, "q","")
            );

            // when
            List<AnswerEmailLine> emailLines = service.saveApplicationAnswers(app, answers);

            // then
            assertThat(emailLines).hasSize(3);
            assertThat(emailLines.get(2).question()).isEqualTo("관심사");
            assertThat(emailLines.get(2).answer()).isEqualTo("(미입력)");

            // 저장 리스트 검증
            ArgumentCaptor<List> cap = ArgumentCaptor.forClass(List.class);
            verify(answerRepository, times(1)).saveAll(cap.capture());
            List saved = cap.getValue();
            assertThat(saved).hasSize(3);
            Answer last = (Answer) saved.get(2);
            assertThat(last.getFormQuestion().getQuestion()).isEqualTo("관심사");
            assertThat(last.getAnswer()).isNull();
        }
    }
}