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

    private final EmailTemplateRenderer renderer;
    private final EmailSender emailSender;
    private final String from;
    private final String subjectPrefix;
    private final ClubMemberRepository  clubMemberRepository;

    public EmailService(
            EmailTemplateRenderer renderer,
            EmailSender emailSender,
            @Value("${email.from}") String from,
            @Value("${email.subject-prefix}") String subjectPrefix,
            ClubMemberRepository clubMemberRepository
    ) {
        this.renderer = renderer;
        this.emailSender = emailSender;
        this.from = from;
        this.subjectPrefix = subjectPrefix;
        this.clubMemberRepository = clubMemberRepository;
    }

    public void sendToApplicant(Application application, List<AnswerEmailLine> emailLines) {

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

        String subject = subjectPrefix + " "
                + application.getClubApplyForm().getClub().getName()
                + " - " + application.getUser().getName();

        User president = clubMemberRepository
                .findUserByClubIdAndRoleAndStatus(application.getClubApplyForm().getClub().getId(), Role.CLUB_ADMIN, ActiveStatus.ACTIVE)
                .orElseThrow(() -> new PresidentNotFoundException("clubId:" + application.getClubApplyForm().getClub().getId()));
        String replyTo = president.getEmail();

        emailSender.sendHtml(from, replyTo,List.of(application.getUser().getEmail()),subject, html);
    }
}
