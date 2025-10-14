package com.kakaotech.team18.backend_server.domain.email.eventListener;

import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.application.repository.ApplicationRepository;
import com.kakaotech.team18.backend_server.domain.email.dto.ApplicationSubmittedEvent;
import com.kakaotech.team18.backend_server.domain.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationNotificationListener {

    private final ApplicationRepository applicationRepository;
    private final EmailService emailService;

    // 트랜잭션이 커밋된 뒤에만 이 메서드가 호출됨
    @Async
    @Transactional(readOnly = true)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSubmitted(ApplicationSubmittedEvent event) {
        Application application = applicationRepository.findById(event.applicationId()).orElse(null);
        if (application == null) return;

        emailService.sendToApplicant(application, event.emailLines());
        log.info("Email sent successfully: clubName={} userName={}", application.getClubApplyForm().getClub().getName(), application.getUser().getName());
    }
}
