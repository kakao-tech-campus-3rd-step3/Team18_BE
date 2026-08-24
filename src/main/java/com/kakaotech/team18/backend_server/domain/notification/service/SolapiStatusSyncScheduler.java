package com.kakaotech.team18.backend_server.domain.notification.service;

import com.kakaotech.team18.backend_server.domain.notification.repository.NotificationDeliveryRepository;
import com.kakaotech.team18.backend_server.domain.notification.service.NotificationDeliveryStateService.AcceptedDelivery;
import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiClientException;
import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiMessageClient;
import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiStatusResponse;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
public class SolapiStatusSyncScheduler {

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    private final NotificationDeliveryRepository repository;
    private final NotificationDeliveryStateService stateService;
    private final SolapiMessageClient messageClient;
    private final int batchSize;
    private final long checkIntervalSeconds;
    private final long maxAcceptedAgeHours;
    private final Clock clock;

    public SolapiStatusSyncScheduler(
            NotificationDeliveryRepository repository,
            NotificationDeliveryStateService stateService,
            SolapiMessageClient messageClient,
            int batchSize,
            long checkIntervalSeconds,
            long maxAcceptedAgeHours
    ) {
        this(
                repository,
                stateService,
                messageClient,
                batchSize,
                checkIntervalSeconds,
                maxAcceptedAgeHours,
                Clock.system(SERVICE_ZONE)
        );
    }

    SolapiStatusSyncScheduler(
            NotificationDeliveryRepository repository,
            NotificationDeliveryStateService stateService,
            SolapiMessageClient messageClient,
            int batchSize,
            long checkIntervalSeconds,
            long maxAcceptedAgeHours,
            Clock clock
    ) {
        if (batchSize <= 0 || checkIntervalSeconds <= 0 || maxAcceptedAgeHours <= 0) {
            throw new IllegalArgumentException("SOLAPI 상태 조회 설정값이 올바르지 않습니다.");
        }
        this.repository = repository;
        this.stateService = stateService;
        this.messageClient = messageClient;
        this.batchSize = batchSize;
        this.checkIntervalSeconds = checkIntervalSeconds;
        this.maxAcceptedAgeHours = maxAcceptedAgeHours;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${notification.status.scheduler-delay-ms:60000}")
    public void synchronizeStatuses() {
        LocalDateTime now = LocalDateTime.now(clock);
        for (Long deliveryId : repository.findDueAcceptedDeliveryIds(
                now,
                PageRequest.of(0, batchSize)
        )) {
            synchronizeOne(deliveryId, now);
        }
    }

    private void synchronizeOne(Long deliveryId, LocalDateTime now) {
        AcceptedDelivery delivery = stateService.findAccepted(deliveryId).orElse(null);
        if (delivery == null) {
            return;
        }
        if (delivery.acceptedAt() == null
                || !delivery.acceptedAt().plusHours(maxAcceptedAgeHours).isAfter(now)) {
            stateService.markAcceptedUnknown(deliveryId, now);
            return;
        }

        LocalDateTime nextCheckAt = now.plusSeconds(checkIntervalSeconds);
        try {
            SolapiStatusResponse response = messageClient.getStatus(delivery.providerMessageId());
            stateService.applyProviderStatus(deliveryId, response, now, nextCheckAt);
        } catch (SolapiClientException exception) {
            stateService.recordStatusCheckFailure(
                    deliveryId,
                    nextCheckAt,
                    exception.getErrorCode(),
                    exception.getMessage()
            );
            log.warn("SOLAPI status lookup failed: deliveryId={} errorCode={}",
                    deliveryId, exception.getErrorCode());
        } catch (RuntimeException exception) {
            stateService.recordStatusCheckFailure(
                    deliveryId,
                    nextCheckAt,
                    "SOLAPI_STATUS_UNEXPECTED_ERROR",
                    safeMessage(exception)
            );
            log.warn("SOLAPI status lookup failed unexpectedly: deliveryId={}",
                    deliveryId, exception);
        }
    }

    private String safeMessage(RuntimeException exception) {
        return exception.getMessage() == null
                ? exception.getClass().getSimpleName()
                : exception.getMessage();
    }
}
