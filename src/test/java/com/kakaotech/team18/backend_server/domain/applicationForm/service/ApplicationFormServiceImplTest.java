package com.kakaotech.team18.backend_server.domain.applicationForm.service;


import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.applicationForm.entity.ApplicationForm;
import com.kakaotech.team18.backend_server.domain.applicationForm.repository.ApplicationFormRepository;
import com.kakaotech.team18.backend_server.domain.applicationFormField.entity.ApplicationFormField;
import com.kakaotech.team18.backend_server.domain.applicationFormField.entity.FieldType;
import com.kakaotech.team18.backend_server.domain.applicationFormField.repository.ApplicationFormFieldRepository;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationFormServiceImplTest {

    @Mock
    private ApplicationFormRepository applicationFormRepository;

    @Mock
    private ApplicationFormFieldRepository applicationFormFieldRepository;

    @InjectMocks
    private ApplicationFormServiceImpl applicationFormServiceImpl;

    @Mock
    private Club mockClub;

    @Mock
    private ApplicationForm mockApplicationForm;

    @Mock
    private Application mockApplication;

    private ApplicationForm applicationForm;
    private List<ApplicationFormField> formFields;

    @BeforeEach
    void setUp() {
        applicationForm = new ApplicationForm(100L, mockClub, mockApplication, "카카오 동아리 지원서", "함께 성장할 팀원을 찾습니다.", true);

        ApplicationFormField textQuestion = new ApplicationFormField(1L, mockApplicationForm, "이름", FieldType.TEXT, true, 1L, null);
        ApplicationFormField radioQuestion = new ApplicationFormField(2L, mockApplicationForm, "성별", FieldType.RADIO, true, 2L, List.of("남","여"));
        ApplicationFormField checkboxQuestion = new ApplicationFormField(3L, mockApplicationForm,"면접가능 요일",  FieldType.CHECKBOX, true, 3L, List.of("월","화","수","목","금","토"));

        formFields = List.of(textQuestion, radioQuestion, checkboxQuestion);
    }

    @Test
    @DisplayName(" clubId로 지원서 조회 양식 조회시 Dto로 매핑하여 반환한다")
    void getQuestionFormWithClubId () throws Exception{

        //given
        Long clubId = 100L;

        when(applicationFormRepository.findByClubIdAndIsActiveTrue(clubId)).thenReturn(Optional.of(applicationForm));
        when(applicationFormFieldRepository.findByApplicationFormIdOrderByDisplayOrderAsc(applicationForm.getId())).thenReturn(formFields);

        //when
        var result = applicationFormServiceImpl.getQuestionForm(clubId);

        //then
        assertThat(result.getTitle()).isEqualTo("카카오 동아리 지원서");
        assertThat(result.getDescription()).isEqualTo("함께 성장할 팀원을 찾습니다.");
        assertThat(result.getQuestions()).hasSize(3);

        var firstQuestion = result.getQuestions().get(0);
        assertThat(firstQuestion.questionNum()).isEqualTo(1L);
        assertThat(firstQuestion.question()).isEqualTo("이름");

        var secondQuestion = result.getQuestions().get(1);
        assertThat(secondQuestion.questionNum()).isEqualTo(2L);
        assertThat(secondQuestion.question()).isEqualTo("성별");
        assertThat(secondQuestion.answerList()).containsExactly("남", "여");

        var thirdQuestion = result.getQuestions().get(2);
        assertThat(thirdQuestion.questionNum()).isEqualTo(3L);
        assertThat(thirdQuestion.question()).isEqualTo("면접가능 요일");
        assertThat(thirdQuestion.answerList()).containsExactly("월", "화", "수", "목", "금", "토");

        verify(applicationFormRepository, times(1)).findByClubIdAndIsActiveTrue(clubId);
        verify(applicationFormFieldRepository, times(1)).findByApplicationFormIdOrderByDisplayOrderAsc(applicationForm.getId());

    }

}
