package com.kakaotech.team18.backend_server.domain.email.eventListener;

import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.application.repository.ApplicationRepository;
import com.kakaotech.team18.backend_server.domain.email.dto.ApplicationInfoDto;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.email.dto.ApplicationSubmittedEvent;
import com.kakaotech.team18.backend_server.domain.email.dto.FinalApprovedEvent;
import com.kakaotech.team18.backend_server.domain.email.dto.FinalRejectedEvent;
import com.kakaotech.team18.backend_server.domain.email.dto.InterviewApprovedEvent;
import com.kakaotech.team18.backend_server.domain.email.dto.InterviewRejectedEvent;
import com.kakaotech.team18.backend_server.domain.email.service.EmailService;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationNotificationListener {

    private final ApplicationRepository applicationRepository;
    private final EmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSubmitted(ApplicationSubmittedEvent event) {
        ApplicationInfoDto info = event.info();

        emailService.sendToApplicant(info, event.emailLines());
        log.info("Email sent successfully: clubName={} userName={}", info.clubName(), info.userName());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInterviewApproved(InterviewApprovedEvent event) {
        ApplicationInfoDto info = event.info();
        emailService.sendInterviewApprovedResultToApplicant(info,  event.message());
        log.info("Email sent successfully: clubName={} userName={}", info.clubName(), info.userName());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInterviewRejected(InterviewRejectedEvent event) {
        ApplicationInfoDto info = event.info();
        emailService.sendInterviewRejectedResultToApplicant(info);
        log.info("Email sent successfully: clubName={} userName={}", info.clubName(), info.userName());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFinalApproved(FinalApprovedEvent event) {
        ApplicationInfoDto info = event.info();
        emailService.sendFinalApprovedResultToApplicant(info, event.message());
        log.info("Email sent successfully: clubName={} userName={}", info.clubName(), info.userName());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFinalRejected(FinalRejectedEvent event) {
        ApplicationInfoDto info = event.info();
        emailService.sendFinalRejectedResultToApplicant(info);
        log.info("Email sent successfully: clubName={} userName={}", info.clubName(), info.userName());
    }
}
