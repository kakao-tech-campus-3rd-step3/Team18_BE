package com.kakaotech.team18.backend_server.domain.notification.service;

import com.kakaotech.team18.backend_server.domain.notification.dto.NotificationMessage;
import com.kakaotech.team18.backend_server.domain.notification.dto.NotificationSendResult;
import com.kakaotech.team18.backend_server.domain.notification.entity.NotificationDelivery;
import com.kakaotech.team18.backend_server.domain.notification.repository.NotificationDeliveryRepository;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationDeliveryStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationDeliveryStateService {

    private final NotificationDeliveryRepository notificationDeliveryRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<NotificationMessage> claim(Long deliveryId, LocalDateTime attemptedAt) {
        NotificationDelivery delivery = findForUpdate(deliveryId);
        if (delivery.getStatus() != NotificationDeliveryStatus.PENDING
                || delivery.getNextAttemptAt().isAfter(attemptedAt)) {
            return Optional.empty();
        }

        delivery.startSending(attemptedAt);
        return Optional.of(NotificationMessage.from(delivery));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(Long deliveryId, NotificationSendResult result, LocalDateTime completedAt) {
        NotificationDelivery delivery = findForUpdate(deliveryId);
        switch (result.outcome()) {
            case SENT -> delivery.markSent(result.providerStatusCode(), completedAt);
            case ACCEPTED -> delivery.markAccepted(
                    result.providerGroupId(),
                    result.providerMessageId(),
                    result.providerStatusCode(),
                    completedAt
            );
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reschedule(
            Long deliveryId,
            LocalDateTime retryAt,
            String errorCode,
            String errorMessage
    ) {
        findForUpdate(deliveryId).reschedule(retryAt, errorCode, errorMessage);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failPermanently(
            Long deliveryId,
            LocalDateTime failedAt,
            String errorCode,
            String errorMessage
    ) {
        findForUpdate(deliveryId).markPermanentlyFailed(errorCode, errorMessage, failedAt);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markUnknown(
            Long deliveryId,
            LocalDateTime unknownAt,
            String errorCode,
            String errorMessage
    ) {
        findForUpdate(deliveryId).markUnknown(errorCode, errorMessage, unknownAt);
    }

    private NotificationDelivery findForUpdate(Long deliveryId) {
        return notificationDeliveryRepository.findByIdForUpdate(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("알림 발송 작업을 찾을 수 없습니다: " + deliveryId));
    }
}
