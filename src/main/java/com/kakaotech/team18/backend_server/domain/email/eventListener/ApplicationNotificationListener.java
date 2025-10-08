package com.kakaotech.team18.backend_server.domain.email.eventListener;

import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.application.repository.ApplicationRepository;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.email.dto.ApplicationSubmittedEvent;
import com.kakaotech.team18.backend_server.domain.email.dto.FinalApprovedEvent;
import com.kakaotech.team18.backend_server.domain.email.dto.FinalRejectedEvent;
import com.kakaotech.team18.backend_server.domain.email.dto.InterviewApprovedEvent;
import com.kakaotech.team18.backend_server.domain.email.dto.InterviewRejectedEvent;
import com.kakaotech.team18.backend_server.domain.email.service.EmailService;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
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
        Club club = application.getClubApplyForm().getClub();
        User user = application.getUser();
        emailService.sendInterviewApprovedResultToApplicant(club, user,  event.message());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInterviewRejected(InterviewRejectedEvent event) {
        Club club = event.club();
        User user = event.user();
        emailService.sendInterviewRejectedResultToApplicant(club, user);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFinalApproved(FinalApprovedEvent event) {
        Application application = applicationRepository.findById(event.applicationId()).orElse(null);
        if (application == null) return;
        Club club = application.getClubApplyForm().getClub();
        User user = application.getUser();
        emailService.sendFinalApprovedResultToApplicant(club, user, event.message());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFinalRejected(FinalRejectedEvent event) {
        Club club = event.club();
        User user = event.user();
        emailService.sendFinalRejectedResultToApplicant(club, user);
    }
}
