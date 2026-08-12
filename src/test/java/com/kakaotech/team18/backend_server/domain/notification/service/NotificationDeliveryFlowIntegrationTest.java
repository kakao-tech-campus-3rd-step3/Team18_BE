package com.kakaotech.team18.backend_server.domain.notification.service;

import com.kakaotech.team18.backend_server.domain.notification.dto.NotificationMessage;
import com.kakaotech.team18.backend_server.domain.notification.dto.NotificationSendResult;
import com.kakaotech.team18.backend_server.domain.notification.entity.NotificationDelivery;
import com.kakaotech.team18.backend_server.domain.notification.repository.NotificationDeliveryRepository;
import com.kakaotech.team18.backend_server.domain.notification.sender.NotificationSender;
import com.kakaotech.team18.backend_server.domain.notification.sender.NotificationSenderRegistry;
import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiMessageStatus;
import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiStatusResponse;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationChannel;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationDeliveryStatus;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationResultType;
import com.kakaotech.team18.backend_server.global.config.JpaAuditingConfig;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaAuditingConfig.class, NotificationDeliveryStateService.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class NotificationDeliveryFlowIntegrationTest {

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-08-12T04:00:00Z"),
            SERVICE_ZONE
    );

    @Autowired
    private NotificationDeliveryRepository repository;

    @Autowired
    private NotificationDeliveryStateService stateService;

    @AfterEach
    void cleanUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("저장된 SMS 작업을 SOLAPI에 한 번 접수하고 최종 성공 상태로 확정한다")
    void processAcceptedSmsAndSynchronizeSentStatus() {
        LocalDateTime now = LocalDateTime.now(CLOCK);
        NotificationDelivery delivery = repository.save(NotificationDelivery.pending(
                1L,
                2L,
                3L,
                "integration-idempotency-key",
                NotificationChannel.SMS,
                NotificationResultType.FINAL_APPROVED,
                "01012345678",
                null,
                null,
                "최종 합격을 축하드립니다.",
                now
        ));
        AtomicInteger calls = new AtomicInteger();
        NotificationSender sender = acceptedSmsSender(calls);
        NotificationDeliveryProcessor processor = new NotificationDeliveryProcessor(
                stateService,
                new NotificationSenderRegistry(List.of(sender)),
                new NotificationRetryPolicy(5, 60, 3_600),
                CLOCK
        );

        processor.process(delivery.getId());

        NotificationDelivery accepted = repository.findById(delivery.getId()).orElseThrow();
        assertThat(calls).hasValue(1);
        assertThat(accepted.getStatus()).isEqualTo(NotificationDeliveryStatus.ACCEPTED);
        assertThat(accepted.getProviderMessageId()).isEqualTo("message-1");

        stateService.applyProviderStatus(
                delivery.getId(),
                new SolapiStatusResponse(SolapiMessageStatus.SENT, "COMPLETE"),
                now.plusMinutes(1),
                now.plusMinutes(2)
        );

        NotificationDelivery sent = repository.findById(delivery.getId()).orElseThrow();
        assertThat(sent.getStatus()).isEqualTo(NotificationDeliveryStatus.SENT);
        assertThat(sent.getSentAt()).isEqualTo(now.plusMinutes(1));
        assertThat(calls).hasValue(1);
    }

    private NotificationSender acceptedSmsSender(AtomicInteger calls) {
        return new NotificationSender() {
            @Override
            public NotificationChannel channel() {
                return NotificationChannel.SMS;
            }

            @Override
            public NotificationSendResult send(NotificationMessage message) {
                calls.incrementAndGet();
                return NotificationSendResult.accepted("group-1", "message-1", "PENDING");
            }
        };
    }
}
