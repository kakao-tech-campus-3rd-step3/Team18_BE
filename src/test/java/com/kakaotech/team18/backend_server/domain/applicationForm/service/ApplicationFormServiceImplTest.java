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

import static org.mockito.Mockito.when;

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


}
