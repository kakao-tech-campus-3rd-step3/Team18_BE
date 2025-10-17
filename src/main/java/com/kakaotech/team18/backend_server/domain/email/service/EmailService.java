package com.kakaotech.team18.backend_server.domain.email.service;

import com.kakaotech.team18.backend_server.domain.email.dto.AnswerEmailLine;
import com.kakaotech.team18.backend_server.domain.email.dto.ApplicationInfoDto;
import com.kakaotech.team18.backend_server.domain.email.sender.EmailSender;
import com.kakaotech.team18.backend_server.domain.email.template.EmailTemplateRenderer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private final EmailTemplateRenderer renderer;
    private final EmailSender emailSender;
    private final String from;

    public EmailService(
            EmailTemplateRenderer renderer,
            EmailSender emailSender,
            @Value("${spring.email.from}") String from
    ) {
        this.renderer = renderer;
        this.emailSender = emailSender;
        this.from = from;
    }

    public void sendToApplicant(ApplicationInfoDto info, List<AnswerEmailLine> emailLines) {

        Long clubId = info.clubId();
       String replyTo = info.presidentEmail();

        Map<String, Object> model = new HashMap<>();

        model.put("title", "동아리 지원서");
        model.put("clubName", info.clubName());
        model.put("applicantName", info.userName());
        model.put("studentId", info.studentId());
        model.put("department", info.userDepartment());
        model.put("phoneNumber", info.userPhoneNumber());
        model.put("applicantEmail", info.userEmail());
        model.put("answers", emailLines);
        model.put("submittedAt", info.LastModifiedAt());

        String html = renderer.render("email-body-applicant", model);

        final String subjectPrefix = "[동아리 지원]";
        String subject = subjectPrefix + " "
                + info.clubName()
                + " - " + info.userName();

        emailSender.sendHtml(from, replyTo,List.of(info.userEmail()),subject, html);
    }

    public void sendResult(ApplicationInfoDto info, ResultType type, String message) {
        Long clubId = info.clubId();
        String replyTo = info.presidentEmail();

        boolean approved = isApproved(type);
        if (approved && (message == null || message.isBlank())) {
            throw new IllegalArgumentException("합격 통지 이메일에는 message가 필요합니다.");
        }

        Map<String, Object> model = baseModel(info);
        model.put("title", titleFor(type));
        if (approved) {
            model.put("message", message);
        }

        String templateName = templateFor(type);

        String html = renderer.render(templateName, model);
        final String subjectPrefix = "[동아리 지원]";
        String subject = subjectPrefix + " "
                + info.clubName()
                + " - " + info.userName();

        emailSender.sendHtml(from, replyTo, List.of(info.userEmail()), subject, html);
    }

    private Map<String, Object> baseModel(ApplicationInfoDto info) {
        Map<String, Object> model = new HashMap<>();
        model.put("clubName", info.clubName());
        model.put("applicantName", info.userName());
        model.put("studentId", info.studentId());
        model.put("department", info.userDepartment());
        model.put("phoneNumber", info.userPhoneNumber());
        model.put("applicantEmail", info.userEmail());
        return model;
    }

    private boolean isApproved(ResultType type) {
        return switch (type) {
            case INTERVIEW_APPROVED, FINAL_APPROVED -> true;
            default -> false;
        };
    }

    private String titleFor(ResultType type) {
        return switch (type) {
            case INTERVIEW_APPROVED -> "동아리 면접 일정 공지";
            case INTERVIEW_REJECTED -> "동아리 면접 결과 안내";
            case FINAL_APPROVED -> "동아리 최종 합격을 축하드립니다";
            case FINAL_REJECTED -> "동아리 최종 결과 안내";
        };
    }

    private String templateFor(ResultType type) {
        return switch (type) {
            case INTERVIEW_APPROVED -> "email-body-applicant-InterviewApproved";
            case INTERVIEW_REJECTED -> "email-body-applicant-InterviewRejected";
            case FINAL_APPROVED -> "email-body-applicant-FinalApproved";
            case FINAL_REJECTED -> "email-body-applicant-FinalRejected";
        };
    }

    public void sendInterviewApprovedResultToApplicant(ApplicationInfoDto info, String message) {
        sendResult(info, ResultType.INTERVIEW_APPROVED, message);
    }
    public void sendInterviewRejectedResultToApplicant(ApplicationInfoDto info) {
        sendResult(info, ResultType.INTERVIEW_REJECTED, null);
    }
    public void sendFinalApprovedResultToApplicant(ApplicationInfoDto info, String message) {
        sendResult(info, ResultType.FINAL_APPROVED, message);
    }
    public void sendFinalRejectedResultToApplicant(ApplicationInfoDto info) {
        sendResult(info, ResultType.FINAL_REJECTED, null);
    }
}


