package com.kakaotech.team18.backend_server.domain.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.kakaotech.team18.backend_server.domain.answer.entity.Answer;
import com.kakaotech.team18.backend_server.domain.answer.repository.AnswerRepository;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ActiveStatus;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import com.kakaotech.team18.backend_server.domain.clubMember.repository.ClubMemberRepository;
import com.kakaotech.team18.backend_server.domain.email.dto.ApplicationInfoDto;
import com.kakaotech.team18.backend_server.domain.formQuestion.entity.FieldType;
import com.kakaotech.team18.backend_server.domain.formQuestion.entity.FormQuestion;
import com.kakaotech.team18.backend_server.domain.formQuestion.repository.FormQuestionRepository;
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

import com.kakaotech.team18.backend_server.global.exception.exceptions.InvalidAnswerException;
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

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private AnswerRepository answerRepository;
    @Mock
    private ClubApplyFormRepository clubApplyFormRepository;
    @Mock
    private ClubMemberRepository clubMemberRepository;
    @Mock
    private FormQuestionRepository formQuestionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ApplicationEventPublisher publisher;

    private static JsonNode tn(String s) {
        return JsonNodeFactory.instance.textNode(s);
    }

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
                .question("관심사")
                .fieldType(FieldType.CHECKBOX)
                .isRequired(false)
                .displayOrder(3L)
                .options(List.of("A", "B", "C"))
                .build();

        FormQuestion q4 = FormQuestion.builder()
                .clubApplyForm(form)
                .question("면접 가능 일정")
                .fieldType(FieldType.TIME_SLOT)
                .isRequired(true)
                .displayOrder(4L)
                .build();

        ReflectionTestUtils.setField(q1, "id", 101L);
        ReflectionTestUtils.setField(q2, "id", 102L);
        ReflectionTestUtils.setField(q3, "id", 103L);
        ReflectionTestUtils.setField(q4, "id", 104L);

        return List.of(q1, q2, q3, q4);
    }

    @Nested
    @DisplayName("submitApplication - 신규 제출")
    class CreateFlow {

        @Test
        @DisplayName("필수 문항(displayOrder=3) 누락 시 InvalidAnswerException")
        void requiredMissing_at_disp3_throws() {
            // given
            ClubApplyForm form = mock(ClubApplyForm.class);
            when(form.getId()).thenReturn(11L);
            Application app = Application.builder().user(baseUser).clubApplyForm(form).build();

            when(formQuestionRepository.findByClubApplyFormIdOrderByDisplayOrderAsc(11L))
                    .thenReturn(sampleQuestions(form));

            // disp=2 빠짐
            List<ApplicationApplyRequestDto.AnswerDto> answers = List.of(
                    new ApplicationApplyRequestDto.AnswerDto(0L, "q", tn("자기소개")),
                    new ApplicationApplyRequestDto.AnswerDto(2L, "q", tn("여")),
                    new ApplicationApplyRequestDto.AnswerDto(3L, "면접", JsonNodeFactory.instance
                            .objectNode().put("interviewDateAnswer", "2025-10-15 14:00"))
            );

            // when / then
            assertThatThrownBy(() ->
                    service.saveApplicationAnswers(app, answers)
            ).isInstanceOf(InvalidAnswerException.class)
                    .satisfies(t -> {
                        InvalidAnswerException ex = (InvalidAnswerException) t;
                        assertThat(ex.getDetail()).contains("displayOrder=2");
                    });
        }


        @Test
        @DisplayName("기존 지원서가 없으면 저장 후 이벤트 발행")
        void create_thenPublishEvent() {
            // given
            Club club = mock(Club.class);
            when(club.getId()).thenReturn(1L);
            when(club.getName()).thenReturn("테스트클럽");

            ClubApplyForm form = mock(ClubApplyForm.class);
            when(form.getId()).thenReturn(11L);
            when(form.getClub()).thenReturn(club);

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

            User president = mock(User.class);
            when(president.getEmail()).thenReturn("president@example.com");
            when(clubMemberRepository.findUserByClubIdAndRoleAndStatus(1L, Role.CLUB_ADMIN, ActiveStatus.ACTIVE))
                    .thenReturn(Optional.of(president));

            ApplicationApplyRequestDto req = new ApplicationApplyRequestDto(
                    "stud@example.com", "홍길동", "20231234", "010-0000-0000", "컴공",
                    List.of(
                            new ApplicationApplyRequestDto.AnswerDto(0L, "q", tn("안녕하세요")),
                            new ApplicationApplyRequestDto.AnswerDto(1L, "q", tn("여")),
                            new ApplicationApplyRequestDto.AnswerDto(2L, "q", tn("A,B")),
                            new ApplicationApplyRequestDto.AnswerDto(3L, "면접 가능 일정",
                                    JsonNodeFactory.instance.objectNode().put("interviewDateAnswer", "2025-10-15 14:00"))
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

            ArgumentCaptor<ApplicationSubmittedEvent> captor =
                    ArgumentCaptor.forClass(ApplicationSubmittedEvent.class);
            verify(publisher, times(1)).publishEvent(captor.capture());
            ApplicationSubmittedEvent event = captor.getValue();

            assertThat(event.applicationId()).isEqualTo(100L);
            assertThat(event.emailLines()).hasSize(4);

            //dto 검사
            ApplicationInfoDto info = event.info();
            assertThat(info.clubId()).isEqualTo(1L);
            assertThat(info.clubName()).isEqualTo("테스트클럽");
            assertThat(info.userName()).isEqualTo("홍길동");
            assertThat(info.studentId()).isEqualTo("20231234");
            assertThat(info.userDepartment()).isEqualTo("컴공");
            assertThat(info.userPhoneNumber()).isEqualTo("010-0000-0000");
            assertThat(info.userEmail()).isEqualTo("stud@example.com");
            assertThat(info.presidentEmail()).isEqualTo("president@example.com");
            assertThat(info.lastModifiedAt()).isNotNull();
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
                    "stud@example.com", "홍길동", "20231234", "010-0000-0000", "컴공",
                    List.of(
                            new ApplicationApplyRequestDto.AnswerDto(0L, "q", tn("수정본문")),
                            new ApplicationApplyRequestDto.AnswerDto(1L, "q", tn("남")),
                            new ApplicationApplyRequestDto.AnswerDto(2L, "q", tn("A")),
                            new ApplicationApplyRequestDto.AnswerDto(3L, "면접 가능 일정",
                                    JsonNodeFactory.instance.arrayNode()
                                            .add("2025-10-15 14:00")
                                            .add("2025-10-16 10:00")
                            )
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
            Club club = mock(Club.class);
            when(club.getId()).thenReturn(1L);
            when(club.getName()).thenReturn("테스트클럽");

            ClubApplyForm form = mock(ClubApplyForm.class);
            when(form.getId()).thenReturn(11L);
            when(form.getClub()).thenReturn(club);

            when(clubApplyFormRepository.findByClubIdAndIsActiveTrue(1L))
                    .thenReturn(Optional.of(form));
            when(userRepository.findByStudentId("20231234"))
                    .thenReturn(Optional.of(baseUser));

            Application existing = Application.builder()
                    .user(baseUser)
                    .clubApplyForm(form)
                    .build();
            ReflectionTestUtils.setField(existing, "id", 201L);
            ReflectionTestUtils.setField(existing, "lastModifiedAt", LocalDateTime.now());

            when(applicationRepository.findByStudentIdAndClubApplyForm(eq("20231234"), eq(form)))
                    .thenReturn(Optional.of(existing));

            when(formQuestionRepository.findByClubApplyFormIdOrderByDisplayOrderAsc(11L))
                    .thenReturn(sampleQuestions(form));

            when(answerRepository.saveAll(anyList()))
                    .thenAnswer(inv -> inv.getArgument(0));

            User president = mock(User.class);
            when(president.getEmail()).thenReturn("president@example.com");
            when(clubMemberRepository.findUserByClubIdAndRoleAndStatus(1L, Role.CLUB_ADMIN, ActiveStatus.ACTIVE))
                    .thenReturn(Optional.of(president));

            ApplicationApplyRequestDto req = new ApplicationApplyRequestDto(
                    "stud@example.com", "홍길동", "20231234", "010-0000-0000", "컴퓨터공학과",
                    List.of(
                            new ApplicationApplyRequestDto.AnswerDto(0L, "q", tn("수정본문")),
                            new ApplicationApplyRequestDto.AnswerDto(1L, "q", tn("여")),
                            new ApplicationApplyRequestDto.AnswerDto(2L, "q", tn("B")),
                            new ApplicationApplyRequestDto.AnswerDto(3L, "면접 가능 일정",
                                    JsonNodeFactory.instance.objectNode().put("interviewDateAnswer", "2025-10-15 14:00"))
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
            ApplicationSubmittedEvent event = captor.getValue();

            assertThat(event.applicationId()).isEqualTo(201L);
            assertThat(event.emailLines()).hasSize(4);

            // dto 검증
            ApplicationInfoDto info = event.info();
            assertThat(info.clubId()).isEqualTo(1L);
            assertThat(info.clubName()).isEqualTo("테스트클럽");
            assertThat(info.userName()).isEqualTo("홍길동");
            assertThat(info.studentId()).isEqualTo("20231234");
            assertThat(info.userDepartment()).isEqualTo("컴퓨터공학과");
            assertThat(info.userPhoneNumber()).isEqualTo("010-0000-0000");
            assertThat(info.userEmail()).isEqualTo("stud@example.com");
            assertThat(info.presidentEmail()).isEqualTo("president@example.com");
            assertThat(info.lastModifiedAt()).isNotNull();
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
                        new ApplicationApplyRequestDto.AnswerDto(0L, "q", tn("자소서")),
                        new ApplicationApplyRequestDto.AnswerDto(1L, "q", tn("남")),
                        new ApplicationApplyRequestDto.AnswerDto(2L, "q", tn("")),
                        new ApplicationApplyRequestDto.AnswerDto(3L, "면접 가능 일정",
                                JsonNodeFactory.instance.objectNode().put("interviewDateAnswer", "2025-10-15 14:00")
                        )
                );

                // when
                List<AnswerEmailLine> emailLines = service.saveApplicationAnswers(app, answers);

                // then
                assertThat(emailLines).hasSize(4);
                assertThat(emailLines.get(2).question()).isEqualTo("관심사");
                assertThat(emailLines.get(2).answer()).isEqualTo("(미입력)");

                ArgumentCaptor<List> cap = ArgumentCaptor.forClass(List.class);
                verify(answerRepository, times(1)).saveAll(cap.capture());
                List saved = cap.getValue();
                assertThat(saved).hasSize(4);
                Answer last = (Answer) saved.get(2);
                assertThat(last.getFormQuestion().getQuestion()).isEqualTo("관심사");
                assertThat(last.getAnswer()).isNull();
            }
        }

        @Test
        @DisplayName("JSON 페이로드 → 서비스 실행 → emailLines 변환 검증")
        void parseJsonPayload_throughService_emailLines_ok() throws Exception {

            String jsonPayload = """
                    {"email":"gjw0622@gmail.com",
                    "name":"카카오톡",
                    "studentId":"234460",
                    "phoneNumber":"010-9045-7394",
                    "department":"컴공",
                    "answers":[
                    {"questionNum":0,"question":"자기소개","answer":"hello"},
                    {"questionNum":1,"question":"성별","answer":"남"},
                    {"questionNum":2,"question":"관심사","answer":"A"},
                    {"questionNum":3,"question":"면접 가능 일정","answer":{"interviewDateAnswer":[{"date":"2025-10-15","selectedTimes":["10:00-10:30","11:30-12:00"]}]}}]}
                    """;

            ObjectMapper om = new ObjectMapper();
            JsonNode root = om.readTree(jsonPayload.getBytes(StandardCharsets.UTF_8));

            List<ApplicationApplyRequestDto.AnswerDto> answers = new ArrayList<>();
            for (JsonNode a : root.get("answers")) {
                long qnum = a.get("questionNum").asLong();
                String qtext = a.get("question").asText();
                JsonNode ans = a.get("answer");
                answers.add(new ApplicationApplyRequestDto.AnswerDto(qnum, qtext, ans));
            }

            ClubApplyForm form = mock(ClubApplyForm.class);
            when(form.getId()).thenReturn(11L);

            User user = User.builder()
                    .studentId(root.get("studentId").asText())
                    .email(root.get("email").asText())
                    .name(root.get("name").asText())
                    .phoneNumber(root.get("phoneNumber").asText())
                    .department(root.get("department").asText())
                    .build();

            Application app = Application.builder()
                    .user(user)
                    .clubApplyForm(form)
                    .build();
            ReflectionTestUtils.setField(app, "id", 999L);
            ReflectionTestUtils.setField(app, "lastModifiedAt", LocalDateTime.now());

            when(formQuestionRepository.findByClubApplyFormIdOrderByDisplayOrderAsc(11L))
                    .thenReturn(sampleQuestions(form));
            when(answerRepository.saveAll(anyList()))
                    .thenAnswer(inv -> inv.getArgument(0));

            List<AnswerEmailLine> emailLines = service.saveApplicationAnswers(app, answers);

            assertThat(emailLines).hasSize(4);

            assertThat(emailLines.get(0).question()).isEqualTo("자기소개");
            assertThat(emailLines.get(0).answer()).isEqualTo("hello");

            assertThat(emailLines.get(1).question()).isEqualTo("성별");
            assertThat(emailLines.get(1).answer()).isEqualTo("남");

            assertThat(emailLines.get(2).question()).isEqualTo("관심사");
            assertThat(emailLines.get(2).answer()).isEqualTo("A");

            assertThat(emailLines.get(3).question()).isEqualTo("면접 가능 일정");
            String ts = emailLines.get(3).answer();
            assertThat(ts).contains("2025-10-15 10:00-10:30");
            assertThat(ts).contains("2025-10-15 11:30-12:00");

            // 저장 엔티티 검증
            @SuppressWarnings("rawtypes")
            ArgumentCaptor<List> cap = ArgumentCaptor.forClass(List.class);
            verify(answerRepository, times(1)).saveAll(cap.capture());
            @SuppressWarnings("unchecked")
            List<Answer> saved = cap.getValue();
            assertThat(saved).hasSize(4);
        }
    }
}