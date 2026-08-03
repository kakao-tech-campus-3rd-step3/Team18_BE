package com.kakaotech.team18.backend_server.domain.notification.entity;

import com.kakaotech.team18.backend_server.domain.BaseEntity;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationChannel;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationDeliveryStatus;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationResultType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "notification_delivery",
        indexes = {
                @Index(
                        name = "idx_notification_delivery_dispatch",
                        columnList = "status,next_attempt_at,notification_delivery_id"
                ),
                @Index(name = "idx_notification_delivery_club_created", columnList = "club_id,created_at"),
                @Index(name = "idx_notification_delivery_created", columnList = "created_at")
        }
)
public class NotificationDelivery extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_delivery_id")
    private Long id;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "club_id", nullable = false)
    private Long clubId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "result_type", nullable = false, length = 40)
    private NotificationResultType resultType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private NotificationDeliveryStatus status;

    @Column(name = "recipient_address", nullable = false, length = 320)
    private String recipientAddress;

    @Column(name = "message_subject", length = 255)
    private String messageSubject;

    @Column(name = "message_body", nullable = false, columnDefinition = "TEXT")
    private String messageBody;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "next_attempt_at", nullable = false)
    private LocalDateTime nextAttemptAt;

    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "unknown_at")
    private LocalDateTime unknownAt;

    @Column(name = "provider_group_id", length = 100)
    private String providerGroupId;

    @Column(name = "provider_message_id", unique = true, length = 100)
    private String providerMessageId;

    @Column(name = "provider_status_code", length = 40)
    private String providerStatusCode;

    @Column(name = "provider_error_code", length = 100)
    private String providerErrorCode;

    @Column(name = "last_error_message", length = 1000)
    private String lastErrorMessage;

    @Builder(access = AccessLevel.PRIVATE)
    private NotificationDelivery(
            Long clubId,
            Long userId,
            Long applicationId,
            NotificationChannel channel,
            NotificationResultType resultType,
            String recipientAddress,
            String messageSubject,
            String messageBody,
            LocalDateTime nextAttemptAt
    ) {
        this.clubId = clubId;
        this.userId = userId;
        this.applicationId = applicationId;
        this.channel = channel;
        this.resultType = resultType;
        this.status = NotificationDeliveryStatus.PENDING;
        this.recipientAddress = recipientAddress;
        this.messageSubject = messageSubject;
        this.messageBody = messageBody;
        this.attemptCount = 0;
        this.nextAttemptAt = nextAttemptAt;
    }

    public static NotificationDelivery pending(
            Long clubId,
            Long userId,
            Long applicationId,
            NotificationChannel channel,
            NotificationResultType resultType,
            String recipientAddress,
            String messageSubject,
            String messageBody,
            LocalDateTime nextAttemptAt
    ) {
        return NotificationDelivery.builder()
                .clubId(clubId)
                .userId(userId)
                .applicationId(applicationId)
                .channel(channel)
                .resultType(resultType)
                .recipientAddress(recipientAddress)
                .messageSubject(messageSubject)
                .messageBody(messageBody)
                .nextAttemptAt(nextAttemptAt)
                .build();
    }

    public void startSending(LocalDateTime attemptedAt) {
        requireStatus(NotificationDeliveryStatus.PENDING);
        status = NotificationDeliveryStatus.SENDING;
        attemptCount++;
        lastAttemptAt = attemptedAt;
    }

    public void reschedule(LocalDateTime retryAt, String errorCode, String errorMessage) {
        requireStatus(NotificationDeliveryStatus.SENDING);
        status = NotificationDeliveryStatus.PENDING;
        nextAttemptAt = retryAt;
        providerErrorCode = errorCode;
        lastErrorMessage = errorMessage;
    }

    public void markAccepted(
            String groupId,
            String messageId,
            String statusCode,
            LocalDateTime acceptedAt
    ) {
        requireStatus(NotificationDeliveryStatus.SENDING);
        status = NotificationDeliveryStatus.ACCEPTED;
        providerGroupId = groupId;
        providerMessageId = messageId;
        providerStatusCode = statusCode;
        this.acceptedAt = acceptedAt;
        clearError();
    }

    public void markSent(String statusCode, LocalDateTime sentAt) {
        requireOneOf(NotificationDeliveryStatus.SENDING, NotificationDeliveryStatus.ACCEPTED);
        status = NotificationDeliveryStatus.SENT;
        providerStatusCode = statusCode;
        this.sentAt = sentAt;
        clearError();
    }

    public void markFailed(
            String statusCode,
            String errorCode,
            String errorMessage,
            LocalDateTime failedAt
    ) {
        requireStatus(NotificationDeliveryStatus.ACCEPTED);
        status = NotificationDeliveryStatus.FAILED;
        providerStatusCode = statusCode;
        providerErrorCode = errorCode;
        lastErrorMessage = errorMessage;
        this.failedAt = failedAt;
    }

    public void markUnknown(String errorCode, String errorMessage, LocalDateTime unknownAt) {
        requireStatus(NotificationDeliveryStatus.SENDING);
        status = NotificationDeliveryStatus.UNKNOWN;
        providerErrorCode = errorCode;
        lastErrorMessage = errorMessage;
        this.unknownAt = unknownAt;
    }

    public void markPermanentlyFailed(String errorCode, String errorMessage, LocalDateTime failedAt) {
        requireOneOf(NotificationDeliveryStatus.PENDING, NotificationDeliveryStatus.SENDING);
        status = NotificationDeliveryStatus.PERMANENTLY_FAILED;
        providerErrorCode = errorCode;
        lastErrorMessage = errorMessage;
        this.failedAt = failedAt;
    }

    private void clearError() {
        providerErrorCode = null;
        lastErrorMessage = null;
    }

    private void requireStatus(NotificationDeliveryStatus expected) {
        if (status != expected) {
            throw new IllegalStateException("알림 발송 상태를 %s에서 변경할 수 없습니다. expected=%s"
                    .formatted(status, expected));
        }
    }

    private void requireOneOf(NotificationDeliveryStatus first, NotificationDeliveryStatus second) {
        if (status != first && status != second) {
            throw new IllegalStateException("알림 발송 상태를 %s에서 변경할 수 없습니다. expected=%s or %s"
                    .formatted(status, first, second));
        }
    }
}
