package com.kakaotech.team18.backend_server.domain.notification.service;

import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationResultType;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class ResultNotificationTemplate {

    private static final DateTimeFormatter INTERVIEW_SCHEDULE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public String render(
            NotificationResultType resultType,
            String clubName,
            String applicantName,
            String customMessage,
            Application application
    ) {
        String greeting = "[" + clubName + "] " + applicantName + "님, ";
        String body = switch (resultType) {
            case INTERVIEW_APPROVED -> greeting
                    + "면접 합격을 축하드립니다.\n"
                    + "면접 일정: " + interviewSchedule(application);
            case INTERVIEW_REJECTED -> greeting
                    + "지원해 주셔서 감사합니다. 심사 결과 이번 면접에서는 함께하지 못하게 되었습니다.";
            case FINAL_APPROVED -> greeting + "최종 합격을 축하드립니다.";
            case FINAL_REJECTED -> greeting
                    + "지원해 주셔서 감사합니다. 심사 결과 이번 모집에서는 함께하지 못하게 되었습니다.";
        };
        return appendCustomMessage(body, customMessage);
    }

    private String interviewSchedule(Application application) {
        if (application.getInterviewDate() == null || application.getInterviewTime() == null) {
            return "추후 안내";
        }
        return LocalDateTime.of(application.getInterviewDate(), application.getInterviewTime())
                .format(INTERVIEW_SCHEDULE_FORMAT);
    }

    private String appendCustomMessage(String body, String customMessage) {
        return customMessage == null || customMessage.isBlank()
                ? body
                : body + "\n" + customMessage.strip();
    }
}
