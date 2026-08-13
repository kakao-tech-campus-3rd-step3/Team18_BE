package com.kakaotech.team18.backend_server.domain.notification.service;

import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationResultType;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class ResultNotificationTemplateTest {

    private final ResultNotificationTemplate template = new ResultNotificationTemplate();

    @Test
    void rendersInterviewApprovedScheduleAndCustomMessage() {
        Application application = mock(Application.class);
        given(application.getInterviewDate()).willReturn(LocalDate.of(2026, 8, 20));
        given(application.getInterviewTime()).willReturn(LocalTime.of(14, 30));

        String rendered = template.render(
                NotificationResultType.INTERVIEW_APPROVED,
                "개발 동아리",
                "홍길동",
                "학생회관으로 와주세요.",
                application
        );

        assertThat(rendered).contains(
                "[개발 동아리] 홍길동님",
                "면접 합격",
                "2026-08-20 14:30",
                "학생회관으로 와주세요."
        );
    }

    @Test
    void appendsCustomMessageToRejectedResultToo() {
        String rendered = template.render(
                NotificationResultType.FINAL_REJECTED,
                "개발 동아리",
                "홍길동",
                "지원 기록은 안전하게 폐기됩니다.",
                mock(Application.class)
        );

        assertThat(rendered).contains("이번 모집에서는 함께하지 못하게", "지원 기록은 안전하게 폐기됩니다.");
    }
}
