package com.kakaotech.team18.backend_server.domain.notification.service;

import com.kakaotech.team18.backend_server.domain.notification.repository.NotificationDeliveryRepository;
import com.kakaotech.team18.backend_server.domain.notification.service.NotificationDeliveryStateService.FailureAlert;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationDeliveryStatus;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(
        prefix = "notification.monitoring",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class NotificationObservabilityScheduler {

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");
    private static final List<NotificationDeliveryStatus> FAILURE_STATUSES = List.of(
            NotificationDeliveryStatus.FAILED,
            NotificationDeliveryStatus.UNKNOWN,
            NotificationDeliveryStatus.PERMANENTLY_FAILED
    );

    private final NotificationDeliveryRepository repository;
    private final NotificationDeliveryStateService stateService;
    private final int failureAlertBatchSize;
    private final Clock clock;
    private final Map<NotificationDeliveryStatus, AtomicLong> statusGauges;

    @Autowired
    public NotificationObservabilityScheduler(
            NotificationDeliveryRepository repository,
            NotificationDeliveryStateService stateService,
            MeterRegistry meterRegistry,
            @org.springframework.beans.factory.annotation.Value(
                    "${notification.monitoring.failure-alert-batch-size:100}"
            ) int failureAlertBatchSize
    ) {
        this(repository, stateService, meterRegistry, failureAlertBatchSize, Clock.system(SERVICE_ZONE));
    }

    NotificationObservabilityScheduler(
            NotificationDeliveryRepository repository,
            NotificationDeliveryStateService stateService,
            MeterRegistry meterRegistry,
            int failureAlertBatchSize,
            Clock clock
    ) {
        if (failureAlertBatchSize <= 0) {
            throw new IllegalArgumentException("실패 알림 배치 크기는 1 이상이어야 합니다.");
        }
        this.repository = repository;
        this.stateService = stateService;
        this.failureAlertBatchSize = failureAlertBatchSize;
        this.clock = clock;
        this.statusGauges = registerStatusGauges(meterRegistry);
    }

    @Scheduled(fixedDelayString = "${notification.monitoring.scheduler-delay-ms:300000}")
    public void monitor() {
        refreshStatusMetrics();
        alertNewFailures();
    }

    private void refreshStatusMetrics() {
        statusGauges.forEach((status, value) -> value.set(repository.countByStatus(status)));
    }

    private void alertNewFailures() {
        LocalDateTime now = LocalDateTime.now(clock);
        List<FailureAlert> failures = new ArrayList<>();
        for (Long deliveryId : repository.findUnalertedFailureIds(
                FAILURE_STATUSES,
                PageRequest.of(0, failureAlertBatchSize)
        )) {
            stateService.claimFailureAlert(deliveryId, now).ifPresent(failures::add);
        }
        if (failures.isEmpty()) {
            return;
        }

        Map<NotificationDeliveryStatus, Long> counts = new EnumMap<>(NotificationDeliveryStatus.class);
        failures.forEach(failure -> counts.merge(failure.status(), 1L, Long::sum));
        String references = failures.stream()
                .map(failure -> "%d:%s:%s:%s".formatted(
                        failure.deliveryId(),
                        failure.channel(),
                        failure.status(),
                        safeErrorCode(failure.errorCode())
                ))
                .toList()
                .toString();
        log.error("Result notification failures detected: counts={} references={}", counts, references);
    }

    private Map<NotificationDeliveryStatus, AtomicLong> registerStatusGauges(MeterRegistry registry) {
        Map<NotificationDeliveryStatus, AtomicLong> gauges =
                new EnumMap<>(NotificationDeliveryStatus.class);
        for (NotificationDeliveryStatus status : NotificationDeliveryStatus.values()) {
            AtomicLong value = new AtomicLong();
            Gauge.builder("notification.delivery.status", value, AtomicLong::get)
                    .description("Current number of result notification deliveries by status")
                    .tag("status", status.name())
                    .register(registry);
            gauges.put(status, value);
        }
        return gauges;
    }

    private String safeErrorCode(String errorCode) {
        return errorCode == null || errorCode.isBlank() ? "NONE" : errorCode;
    }
}
