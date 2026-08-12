package com.kakaotech.team18.backend_server.domain.notification.service;

import com.kakaotech.team18.backend_server.domain.notification.dto.NotificationMessage;
import com.kakaotech.team18.backend_server.domain.notification.dto.NotificationSendResult;
import com.kakaotech.team18.backend_server.domain.notification.exception.NotificationSendException;
import com.kakaotech.team18.backend_server.domain.notification.sender.NotificationSender;
import com.kakaotech.team18.backend_server.domain.notification.sender.NotificationSenderRegistry;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationDeliveryProcessor {

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");
    private static final Duration FIRST_RETRY_DELAY = Duration.ofMinutes(1);

    private final NotificationDeliveryStateService stateService;
    private final NotificationSenderRegistry senderRegistry;
    private final Clock clock;

    @Autowired
    public NotificationDeliveryProcessor(
            NotificationDeliveryStateService stateService,
            NotificationSenderRegistry senderRegistry
    ) {
        this(stateService, senderRegistry, Clock.system(SERVICE_ZONE));
    }

    NotificationDeliveryProcessor(
            NotificationDeliveryStateService stateService,
            NotificationSenderRegistry senderRegistry,
            Clock clock
    ) {
        this.stateService = stateService;
        this.senderRegistry = senderRegistry;
        this.clock = clock;
    }

    public void process(Long deliveryId) {
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
            switch (exception.getDisposition()) {
                case RETRYABLE -> stateService.reschedule(
                            deliveryId,
                            now().plus(FIRST_RETRY_DELAY),
                            exception.getErrorCode(),
                            exception.getMessage()
                    );
                case UNKNOWN -> stateService.markUnknown(
                        deliveryId,
                        now(),
                        exception.getErrorCode(),
                        exception.getMessage()
                );
                case PERMANENT -> stateService.failPermanently(
                        deliveryId,
                        now(),
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

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private String safeMessage(RuntimeException exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? exception.getClass().getSimpleName() : message;
    }
}
