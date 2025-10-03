package com.kakaotech.team18.backend_server.domain.email.eventListener;

import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.application.repository.ApplicationRepository;
import com.kakaotech.team18.backend_server.domain.email.dto.ApplicationSubmittedEvent;
import com.kakaotech.team18.backend_server.domain.email.dto.FinalApprovedEvent;
import com.kakaotech.team18.backend_server.domain.email.dto.FinalRejectedEvent;
import com.kakaotech.team18.backend_server.domain.email.dto.InterviewApprovedEvent;
import com.kakaotech.team18.backend_server.domain.email.dto.InterviewRejectedEvent;
import com.kakaotech.team18.backend_server.domain.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ApplicationNotificationListener {

    private final ApplicationRepository applicationRepository;
    private final EmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSubmitted(ApplicationSubmittedEvent event) {
        Application application = applicationRepository.findById(event.applicationId()).orElse(null);
        if (application == null) return;

        emailService.sendToApplicant(application, event.emailLines());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInterviewApproved(InterviewApprovedEvent event) {
        Application application = applicationRepository.findById(event.applicationId()).orElse(null);
        if (application == null) return;

        emailService.sendInterviewApprovedResultToApplicant(application, event.message());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInterviewRejected(InterviewRejectedEvent event) {
        Application application = applicationRepository.findById(event.applicationId()).orElse(null);
        if (application == null) return;

        emailService.sendInterviewRejectedResultToApplicant(application);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFinalApproved(FinalApprovedEvent event) {
        Application application = applicationRepository.findById(event.applicationId()).orElse(null);
        if (application == null) return;

        emailService.sendFinalApprovedResultToApplicant(application, event.message());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFinalRejected(FinalRejectedEvent event) {
        Application application = applicationRepository.findById(event.applicationId()).orElse(null);
        if (application == null) return;

        emailService.sendFinalRejectedResultToApplicant(application);
    }
}
