package com.kakaotech.team18.backend_server.domain.notification.event;

import com.kakaotech.team18.backend_server.domain.notification.service.NotificationDeliveryProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "notification.dispatch",
        name = "enabled",
        havingValue = "true"
)
public class ResultNotificationDispatchListener {

    private final NotificationDeliveryProcessor processor;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDispatchRequested(ResultNotificationDispatchRequestedEvent event) {
        for (Long deliveryId : event.deliveryIds()) {
            try {
                processor.process(deliveryId);
            } catch (RuntimeException exception) {
                log.error("Notification delivery processing failed unexpectedly: deliveryId={}", deliveryId, exception);
            }
        }
    }
}
