package com.kakaotech.team18.backend_server.domain.notification.service;

import com.kakaotech.team18.backend_server.domain.notification.repository.NotificationDeliveryRepository;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationDeliveryStatus;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
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
        prefix = "notification.retention",
        name = "enabled",
        havingValue = "true"
)
public class NotificationRetentionScheduler {

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");
    private static final List<NotificationDeliveryStatus> TERMINAL_STATUSES = List.of(
            NotificationDeliveryStatus.SENT,
            NotificationDeliveryStatus.FAILED,
            NotificationDeliveryStatus.UNKNOWN,
            NotificationDeliveryStatus.PERMANENTLY_FAILED
    );

    private final NotificationDeliveryRepository repository;
    private final NotificationDeliveryStateService stateService;
    private final int retentionDays;
    private final int batchSize;
    private final Clock clock;

    @Autowired
    public NotificationRetentionScheduler(
            NotificationDeliveryRepository repository,
            NotificationDeliveryStateService stateService,
            @Value("${notification.retention.days:90}") int retentionDays,
            @Value("${notification.retention.batch-size:100}") int batchSize
    ) {
        this(repository, stateService, retentionDays, batchSize, Clock.system(SERVICE_ZONE));
    }

    NotificationRetentionScheduler(
            NotificationDeliveryRepository repository,
            NotificationDeliveryStateService stateService,
            int retentionDays,
            int batchSize,
            Clock clock
    ) {
        if (retentionDays <= 0 || batchSize <= 0) {
            throw new IllegalArgumentException("알림 개인정보 보관 설정값은 1 이상이어야 합니다.");
        }
        this.repository = repository;
        this.stateService = stateService;
        this.retentionDays = retentionDays;
        this.batchSize = batchSize;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${notification.retention.scheduler-delay-ms:86400000}")
    public void redactExpiredSensitiveData() {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime cutoff = now.minusDays(retentionDays);
        int redactedCount = 0;
        for (Long deliveryId : repository.findRetentionTargetIds(
                TERMINAL_STATUSES,
                cutoff,
                PageRequest.of(0, batchSize)
        )) {
            try {
                if (stateService.redactSensitiveData(deliveryId, now)) {
                    redactedCount++;
                }
            } catch (RuntimeException exception) {
                log.error("Notification sensitive data redaction failed: deliveryId={}",
                        deliveryId, exception);
            }
        }
        if (redactedCount > 0) {
            log.info("Notification sensitive data redacted: count={} cutoff={}",
                    redactedCount, cutoff);
        }
    }
}
