package com.kakaotech.team18.backend_server.domain.notification.service;

import com.kakaotech.team18.backend_server.domain.notification.dto.NotificationMessage;
import com.kakaotech.team18.backend_server.domain.notification.dto.NotificationSendResult;
import com.kakaotech.team18.backend_server.domain.notification.exception.NotificationSendException;
import com.kakaotech.team18.backend_server.domain.notification.sender.NotificationSender;
import com.kakaotech.team18.backend_server.domain.notification.sender.NotificationSenderRegistry;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationDeliveryProcessor {

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");
    private final NotificationDeliveryStateService stateService;
    private final NotificationSenderRegistry senderRegistry;
    private final NotificationRetryPolicy retryPolicy;
    private final Clock clock;

    @Autowired
    public NotificationDeliveryProcessor(
            NotificationDeliveryStateService stateService,
            NotificationSenderRegistry senderRegistry,
            NotificationRetryPolicy retryPolicy
    ) {
        this(stateService, senderRegistry, retryPolicy, Clock.system(SERVICE_ZONE));
    }

    NotificationDeliveryProcessor(
            NotificationDeliveryStateService stateService,
            NotificationSenderRegistry senderRegistry,
            NotificationRetryPolicy retryPolicy,
            Clock clock
    ) {
        this.stateService = stateService;
        this.senderRegistry = senderRegistry;
        this.retryPolicy = retryPolicy;
        this.clock = clock;
    }

    public void process(Long deliveryId) {
        if (stateService.findChannel(deliveryId)
                .filter(senderRegistry::supports)
                .isEmpty()) {
            return;
        }
        LocalDateTime attemptedAt = now();
        Optional<NotificationMessage> claimed = stateService.claim(deliveryId, attemptedAt);
        if (claimed.isEmpty()) {
            return;
        }

        NotificationMessage message = claimed.get();
        try {
            NotificationSender sender = senderRegistry.get(message.channel());
            NotificationSendResult result = sender.send(message);
            stateService.complete(deliveryId, result, now());
        } catch (NotificationSendException exception) {
            LocalDateTime failedAt = now();
            switch (exception.getDisposition()) {
                case RETRYABLE -> handleRetryable(deliveryId, message, exception, failedAt);
                case UNKNOWN -> stateService.markUnknown(
                        deliveryId,
                        failedAt,
                        exception.getErrorCode(),
                        exception.getMessage()
                );
                case PERMANENT -> stateService.failPermanently(
                        deliveryId,
                        failedAt,
                        exception.getErrorCode(),
                        exception.getMessage()
                );
            }
        } catch (RuntimeException exception) {
            stateService.failPermanently(
                    deliveryId,
                    now(),
                    "UNEXPECTED_NOTIFICATION_ERROR",
                    safeMessage(exception)
            );
        }
    }

    private void handleRetryable(
            Long deliveryId,
            NotificationMessage message,
            NotificationSendException exception,
            LocalDateTime failedAt
    ) {
        if (retryPolicy.exhausted(message.attemptCount(), exception.getErrorCode())) {
            stateService.failPermanently(
                    deliveryId,
                    failedAt,
                    exception.getErrorCode(),
                    "최대 재시도 횟수에 도달했습니다: " + exception.getMessage()
            );
            return;
        }
        LocalDateTime retryAt = exception.getRetryAt() == null
                ? retryPolicy.nextRetryAt(failedAt, message.attemptCount())
                : exception.getRetryAt();
        stateService.reschedule(
                deliveryId,
                retryAt,
                exception.getErrorCode(),
                exception.getMessage()
        );
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private String safeMessage(RuntimeException exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? exception.getClass().getSimpleName() : message;
    }
}
