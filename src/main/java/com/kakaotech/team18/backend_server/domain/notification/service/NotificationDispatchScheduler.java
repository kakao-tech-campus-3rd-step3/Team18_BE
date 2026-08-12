package com.kakaotech.team18.backend_server.domain.notification.service;

import com.kakaotech.team18.backend_server.domain.notification.repository.NotificationDeliveryRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(
        prefix = "notification.dispatch",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class NotificationDispatchScheduler {

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    private final NotificationDeliveryRepository repository;
    private final NotificationDeliveryProcessor processor;
    private final NotificationDeliveryStateService stateService;
    private final int batchSize;
    private final long sendingTimeoutSeconds;
    private final Clock clock;

    @Autowired
    public NotificationDispatchScheduler(
            NotificationDeliveryRepository repository,
            NotificationDeliveryProcessor processor,
            NotificationDeliveryStateService stateService,
            @Value("${notification.dispatch.batch-size:50}") int batchSize,
            @Value("${notification.dispatch.sending-timeout-seconds:600}") long sendingTimeoutSeconds
    ) {
        this(
                repository,
                processor,
                stateService,
                batchSize,
                sendingTimeoutSeconds,
                Clock.system(SERVICE_ZONE)
        );
    }

    NotificationDispatchScheduler(
            NotificationDeliveryRepository repository,
            NotificationDeliveryProcessor processor,
            NotificationDeliveryStateService stateService,
            int batchSize,
            long sendingTimeoutSeconds,
            Clock clock
    ) {
        if (batchSize <= 0 || sendingTimeoutSeconds <= 0) {
            throw new IllegalArgumentException("알림 스케줄러 설정값이 올바르지 않습니다.");
        }
        this.repository = repository;
        this.processor = processor;
        this.stateService = stateService;
        this.batchSize = batchSize;
        this.sendingTimeoutSeconds = sendingTimeoutSeconds;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${notification.dispatch.scheduler-delay-ms:30000}")
    public void dispatchAndRecover() {
        LocalDateTime now = LocalDateTime.now(clock);
        PageRequest batch = PageRequest.of(0, batchSize);

        for (Long deliveryId : repository.findDueDeliveryIds(now, batch)) {
            try {
                processor.process(deliveryId);
            } catch (RuntimeException exception) {
                log.error("Scheduled notification processing failed: deliveryId={}",
                        deliveryId, exception);
            }
        }

        LocalDateTime cutoff = now.minusSeconds(sendingTimeoutSeconds);
        for (Long deliveryId : repository.findStaleSendingDeliveryIds(cutoff, batch)) {
            try {
                stateService.markStaleSendingUnknown(deliveryId, cutoff, now);
            } catch (RuntimeException exception) {
                log.error("Stale notification recovery failed: deliveryId={}",
                        deliveryId, exception);
            }
        }
    }
}
