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
}


