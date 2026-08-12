package com.kakaotech.team18.backend_server.domain.notification.service;

import com.kakaotech.team18.backend_server.domain.notification.dto.NotificationMessage;
import com.kakaotech.team18.backend_server.domain.notification.dto.NotificationSendResult;
import com.kakaotech.team18.backend_server.domain.notification.entity.NotificationDelivery;
import com.kakaotech.team18.backend_server.domain.notification.repository.NotificationDeliveryRepository;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationDeliveryStatus;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationChannel;
import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiMessageStatus;
import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiStatusResponse;
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

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public Optional<NotificationChannel> findChannel(Long deliveryId) {
        return notificationDeliveryRepository.findById(deliveryId)
                .map(NotificationDelivery::getChannel);
    }

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
                    completedAt,
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean markStaleSendingUnknown(
            Long deliveryId,
            LocalDateTime cutoff,
            LocalDateTime unknownAt
    ) {
        NotificationDelivery delivery = findForUpdate(deliveryId);
        if (delivery.getStatus() != NotificationDeliveryStatus.SENDING
                || delivery.getLastAttemptAt() == null
                || delivery.getLastAttemptAt().isAfter(cutoff)) {
            return false;
        }
        delivery.markUnknown(
                "DISPATCH_INTERRUPTED",
                "발송 처리 도중 서버가 중단되어 외부 접수 여부를 확인할 수 없습니다.",
                unknownAt
        );
        return true;
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public Optional<AcceptedDelivery> findAccepted(Long deliveryId) {
        return notificationDeliveryRepository.findById(deliveryId)
                .filter(delivery -> delivery.getStatus() == NotificationDeliveryStatus.ACCEPTED)
                .map(delivery -> new AcceptedDelivery(
                        delivery.getId(),
                        delivery.getProviderMessageId(),
                        delivery.getAcceptedAt()
                ));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean applyProviderStatus(
            Long deliveryId,
            SolapiStatusResponse response,
            LocalDateTime checkedAt,
            LocalDateTime nextCheckAt
    ) {
        NotificationDelivery delivery = findForUpdate(deliveryId);
        if (delivery.getStatus() != NotificationDeliveryStatus.ACCEPTED) {
            return false;
        }
        if (response.status() == SolapiMessageStatus.SENT) {
            delivery.markSent(response.providerStatusCode(), checkedAt);
        } else if (response.status() == SolapiMessageStatus.FAILED) {
            delivery.markFailed(
                    response.providerStatusCode(),
                    "SOLAPI_DELIVERY_FAILED",
                    "통신사 최종 발송에 실패했습니다.",
                    checkedAt
            );
        } else {
            delivery.rescheduleStatusCheck(
                    nextCheckAt,
                    response.providerStatusCode(),
                    null,
                    null
            );
        }
        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean recordStatusCheckFailure(
            Long deliveryId,
            LocalDateTime nextCheckAt,
            String errorCode,
            String errorMessage
    ) {
        NotificationDelivery delivery = findForUpdate(deliveryId);
        if (delivery.getStatus() != NotificationDeliveryStatus.ACCEPTED) {
            return false;
        }
        delivery.rescheduleStatusCheck(
                nextCheckAt,
                delivery.getProviderStatusCode(),
                errorCode,
                errorMessage
        );
        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean markAcceptedUnknown(Long deliveryId, LocalDateTime unknownAt) {
        NotificationDelivery delivery = findForUpdate(deliveryId);
        if (delivery.getStatus() != NotificationDeliveryStatus.ACCEPTED) {
            return false;
        }
        delivery.markUnknown(
                "SOLAPI_STATUS_TIMEOUT",
                "SOLAPI 접수 후 최종 발송 결과를 제한 시간 안에 확인하지 못했습니다.",
                unknownAt
        );
        return true;
    }

    public record AcceptedDelivery(
            Long deliveryId,
            String providerMessageId,
            LocalDateTime acceptedAt
    ) {
    }

    private NotificationDelivery findForUpdate(Long deliveryId) {
        return notificationDeliveryRepository.findByIdForUpdate(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("알림 발송 작업을 찾을 수 없습니다: " + deliveryId));
    }
}
