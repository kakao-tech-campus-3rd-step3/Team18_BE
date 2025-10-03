package com.kakaotech.team18.backend_server.domain.email.service;

import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ActiveStatus;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import com.kakaotech.team18.backend_server.domain.clubMember.repository.ClubMemberRepository;
import com.kakaotech.team18.backend_server.domain.email.dto.AnswerEmailLine;
import com.kakaotech.team18.backend_server.domain.email.sender.EmailSender;
import com.kakaotech.team18.backend_server.domain.email.template.EmailTemplateRenderer;

import com.kakaotech.team18.backend_server.domain.user.entity.User;
import com.kakaotech.team18.backend_server.global.exception.exceptions.PresidentNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private static final String SUBJECT_PREFIX = "[동아리 지원]";

    private final EmailTemplateRenderer renderer;
    private final EmailSender emailSender;
    private final String from;
    private final ClubMemberRepository  clubMemberRepository;

    public EmailService(
            EmailTemplateRenderer renderer,
            EmailSender emailSender,
            @Value("${spring.email.from}") String from,
            ClubMemberRepository clubMemberRepository
    ) {
        this.renderer = renderer;
        this.emailSender = emailSender;
        this.from = from;
        this.clubMemberRepository = clubMemberRepository;
    }

    public enum ResultType {
        INTERVIEW_APPROVED, INTERVIEW_REJECTED,
        FINAL_APPROVED, FINAL_REJECTED
    }

    public void sendToApplicant(Application application, List<AnswerEmailLine> emailLines) {

        Long clubId = application.getClubApplyForm().getClub().getId();
        User president = clubMemberRepository
                .findUserByClubIdAndRoleAndStatus(clubId, Role.CLUB_ADMIN, ActiveStatus.ACTIVE)
                .orElseThrow(() -> new PresidentNotFoundException("clubId:" + clubId));
        String replyTo = president.getEmail();

        Map<String, Object> model = new HashMap<>();

        model.put("title", "동아리 지원서");
        model.put("clubName", application.getClubApplyForm().getClub().getName());
        model.put("applicantName", application.getUser().getName());
        model.put("studentId", application.getUser().getStudentId());
        model.put("department", application.getUser().getDepartment());
        model.put("phoneNumber", application.getUser().getPhoneNumber());
        model.put("applicantEmail", application.getUser().getEmail());
        model.put("answers", emailLines);
        model.put("submittedAt", application.getLastModifiedAt());

        String html = renderer.render("email-body-applicant", model);

        final String subjectPrefix = "[동아리 지원]";
        String subject = subjectPrefix + " "
                + application.getClubApplyForm().getClub().getName()
                + " - " + application.getUser().getName();

        emailSender.sendHtml(from, replyTo,List.of(application.getUser().getEmail()),subject, html);
    }

    public void sendResult(Application application, ResultType type, String message) {
        Long clubId = application.getClubApplyForm().getClub().getId();
        User president = clubMemberRepository
                .findUserByClubIdAndRoleAndStatus(clubId, Role.CLUB_ADMIN, ActiveStatus.ACTIVE)
                .orElseThrow(() -> new PresidentNotFoundException("clubId:" + clubId));
        String replyTo = president.getEmail();

        boolean approved = isApproved(type);
        if (approved && (message == null || message.isBlank())) {
            throw new IllegalArgumentException("합격 통지 이메일에는 message가 필요합니다.");
        }

        Map<String, Object> model = baseModel(application);
        model.put("title", titleFor(type));
        if (approved) {
            model.put("message", message);
        }

        String templateName = templateFor(type);

        String html = renderer.render(templateName, model);
        final String subjectPrefix = "[동아리 지원]";
        String subject = subjectPrefix + " "
                + application.getClubApplyForm().getClub().getName()
                + " - " + application.getUser().getName();

        emailSender.sendHtml(from, replyTo, List.of(application.getUser().getEmail()), subject, html);
    }

    private Map<String, Object> baseModel(Application application) {
        Map<String, Object> model = new HashMap<>();
        model.put("clubName", application.getClubApplyForm().getClub().getName());
        model.put("applicantName", application.getUser().getName());
        model.put("studentId", application.getUser().getStudentId());
        model.put("department", application.getUser().getDepartment());
        model.put("phoneNumber", application.getUser().getPhoneNumber());
        model.put("applicantEmail", application.getUser().getEmail());
        model.put("submittedAt", application.getLastModifiedAt());
        return model;
    }

    private String subjectFor(Application application) {

    }

    private boolean isApproved(ResultType type) {
        return switch (type) {
            case INTERVIEW_APPROVED, FINAL_APPROVED -> true;
            default -> false;
        };
    }

    private String titleFor(ResultType type) {
        return switch (type) {
            case INTERVIEW_APPROVED -> "동아리 면접 합격을 축하드립니다";
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

    public void sendInterviewApprovedResultToApplicant(Application application, String message) {
        sendResult(application, ResultType.INTERVIEW_APPROVED, message);
    }
    public void sendInterviewRejectedResultToApplicant(Application application) {
        sendResult(application, ResultType.INTERVIEW_REJECTED, null);
    }
    public void sendFinalApprovedResultToApplicant(Application application, String message) {
        sendResult(application, ResultType.FINAL_APPROVED, message);
    }
    public void sendFinalRejectedResultToApplicant(Application application) {
        sendResult(application, ResultType.FINAL_REJECTED, null);
    }
}
