package com.kakaotech.team18.backend_server.domain.email.eventListener;

import com.kakaotech.team18.backend_server.domain.email.dto.ApplicationInfoDto;
import com.kakaotech.team18.backend_server.domain.email.dto.ApplicationSubmittedEvent;
import com.kakaotech.team18.backend_server.domain.email.dto.FinalApprovedEvent;
import com.kakaotech.team18.backend_server.domain.email.dto.FinalRejectedEvent;
import com.kakaotech.team18.backend_server.domain.email.service.EmailService;
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

    private final EmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSubmitted(ApplicationSubmittedEvent event) {
        ApplicationInfoDto info = event.info();

        emailService.sendToApplicant(info, event.emailLines());
        log.info("Email sent successfully: clubName={} userName={}", info.clubName(), info.userName());
    }

    /** stage가 없는 기존 내부 호출 호환용이며, 신규 결과 발표 API는 DB 발송 작업을 사용합니다. */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLegacyFinalApproved(FinalApprovedEvent event) {
        ApplicationInfoDto info = event.info();
        emailService.sendFinalApprovedResultToApplicant(info, event.message());
        log.info("Legacy result email sent successfully: clubName={} userName={}",
                info.clubName(), info.userName());
    }

    /** stage가 없는 기존 내부 호출 호환용이며, 신규 결과 발표 API는 DB 발송 작업을 사용합니다. */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLegacyFinalRejected(FinalRejectedEvent event) {
        ApplicationInfoDto info = event.info();
        emailService.sendFinalRejectedResultToApplicant(info);
        log.info("Legacy result email sent successfully: clubName={} userName={}",
                info.clubName(), info.userName());
    }
}
