package com.kakaotech.team18.backend_server.domain.notification.entity;

import com.kakaotech.team18.backend_server.domain.BaseEntity;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationChannel;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationDeliveryStatus;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationResultType;
import com.kakaotech.team18.backend_server.domain.notification.sms.SmsMessageType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "notification_delivery",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_notification_delivery_idempotency_recipient",
                columnNames = {"club_id", "idempotency_key", "user_id", "channel", "result_type"}
        ),
        indexes = {
                @Index(
                        name = "idx_notification_delivery_dispatch",
                        columnList = "status,next_attempt_at,notification_delivery_id"
                ),
                @Index(
                        name = "idx_notification_delivery_failure_alert",
                        columnList = "status,failure_alerted_at,notification_delivery_id"
                ),
                @Index(
                        name = "idx_notification_delivery_pending_alert",
                        columnList = "status,pending_alerted_at,created_at,notification_delivery_id"
                ),
                @Index(
                        name = "idx_notification_delivery_retention",
                        columnList = "status,redacted_at,created_at,notification_delivery_id"
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

    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;

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

    @Column(name = "reply_to_address", length = 320)
    private String replyToAddress;

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

    @Column(name = "failure_alerted_at")
    private LocalDateTime failureAlertedAt;

    @Column(name = "pending_alerted_at")
    private LocalDateTime pendingAlertedAt;

    @Column(name = "redacted_at")
    private LocalDateTime redactedAt;

    @Column(name = "provider_group_id", length = 100)
    private String providerGroupId;

    @Column(name = "provider_message_id", unique = true, length = 100)
    private String providerMessageId;

    @Column(name = "provider_status_code", length = 40)
    private String providerStatusCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", length = 20)
    private SmsMessageType messageType;

    @Column(name = "estimated_cost", precision = 14, scale = 4)
    private BigDecimal estimatedCost;

    @Column(name = "provider_error_code", length = 100)
    private String providerErrorCode;

    @Column(name = "last_error_message", length = 1000)
    private String lastErrorMessage;

    @Builder(access = AccessLevel.PRIVATE)
    private NotificationDelivery(
            Long clubId,
            Long userId,
            Long applicationId,
            String idempotencyKey,
            NotificationChannel channel,
            NotificationResultType resultType,
            String recipientAddress,
            String replyToAddress,
            String messageSubject,
            String messageBody,
            LocalDateTime nextAttemptAt
    ) {
        this.clubId = clubId;
        this.userId = userId;
        this.applicationId = applicationId;
        this.idempotencyKey = idempotencyKey;
        this.channel = channel;
        this.resultType = resultType;
        this.status = NotificationDeliveryStatus.PENDING;
        this.recipientAddress = recipientAddress;
        this.replyToAddress = replyToAddress;
        this.messageSubject = messageSubject;
        this.messageBody = messageBody;
        this.attemptCount = 0;
        this.nextAttemptAt = nextAttemptAt;
    }

    public static NotificationDelivery pending(
            Long clubId,
            Long userId,
            Long applicationId,
            String idempotencyKey,
            NotificationChannel channel,
            NotificationResultType resultType,
            String recipientAddress,
            String replyToAddress,
            String messageSubject,
            String messageBody,
            LocalDateTime nextAttemptAt
    ) {
        return NotificationDelivery.builder()
                .clubId(clubId)
                .userId(userId)
                .applicationId(applicationId)
                .idempotencyKey(idempotencyKey)
                .channel(channel)
                .resultType(resultType)
                .recipientAddress(recipientAddress)
                .replyToAddress(replyToAddress)
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
            SmsMessageType messageType,
            BigDecimal estimatedCost,
            LocalDateTime acceptedAt,
            LocalDateTime nextStatusCheckAt
    ) {
        requireStatus(NotificationDeliveryStatus.SENDING);
        status = NotificationDeliveryStatus.ACCEPTED;
        providerGroupId = groupId;
        providerMessageId = messageId;
        providerStatusCode = statusCode;
        this.messageType = messageType;
        this.estimatedCost = estimatedCost;
        this.acceptedAt = acceptedAt;
        this.nextAttemptAt = nextStatusCheckAt;
        clearError();
    }

    public void markAccepted(
            String groupId,
            String messageId,
            String statusCode,
            LocalDateTime acceptedAt,
            LocalDateTime nextStatusCheckAt
    ) {
        markAccepted(
                groupId,
                messageId,
                statusCode,
                null,
                BigDecimal.ZERO,
                acceptedAt,
                nextStatusCheckAt
        );
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
        requireOneOf(NotificationDeliveryStatus.SENDING, NotificationDeliveryStatus.ACCEPTED);
        status = NotificationDeliveryStatus.UNKNOWN;
        providerErrorCode = errorCode;
        lastErrorMessage = errorMessage;
        this.unknownAt = unknownAt;
    }

    public void rescheduleStatusCheck(
            LocalDateTime nextStatusCheckAt,
            String statusCode,
            String errorCode,
            String errorMessage
    ) {
        requireStatus(NotificationDeliveryStatus.ACCEPTED);
        this.nextAttemptAt = nextStatusCheckAt;
        this.providerStatusCode = statusCode;
        this.providerErrorCode = errorCode;
        this.lastErrorMessage = errorMessage;
    }

    public void markPermanentlyFailed(String errorCode, String errorMessage, LocalDateTime failedAt) {
        requireOneOf(NotificationDeliveryStatus.PENDING, NotificationDeliveryStatus.SENDING);
        status = NotificationDeliveryStatus.PERMANENTLY_FAILED;
        providerErrorCode = errorCode;
        lastErrorMessage = errorMessage;
        this.failedAt = failedAt;
    }

    public void markFailureAlerted(LocalDateTime alertedAt) {
        if (status != NotificationDeliveryStatus.FAILED
                && status != NotificationDeliveryStatus.UNKNOWN
                && status != NotificationDeliveryStatus.PERMANENTLY_FAILED) {
            throw new IllegalStateException("최종 실패 상태가 아닌 알림을 보고 처리할 수 없습니다: " + status);
        }
        if (failureAlertedAt != null) {
            throw new IllegalStateException("이미 보고된 알림입니다: " + id);
        }
        failureAlertedAt = alertedAt;
    }

    public void markPendingAlerted(LocalDateTime alertedAt) {
        requireStatus(NotificationDeliveryStatus.PENDING);
        if (pendingAlertedAt != null) {
            throw new IllegalStateException("이미 장기 대기 보고된 알림입니다: " + id);
        }
        pendingAlertedAt = alertedAt;
    }

    public void redactSensitiveData(LocalDateTime redactedAt) {
        if (!isTerminalStatus()) {
            throw new IllegalStateException("종료되지 않은 알림의 민감정보를 비식별화할 수 없습니다: " + status);
        }
        if (this.redactedAt != null) {
            throw new IllegalStateException("이미 민감정보가 비식별화된 알림입니다: " + id);
        }
        recipientAddress = "[REDACTED]";
        replyToAddress = null;
        messageSubject = null;
        messageBody = "[REDACTED]";
        lastErrorMessage = null;
        this.redactedAt = redactedAt;
    }

    private void clearError() {
        providerErrorCode = null;
        lastErrorMessage = null;
    }

    private boolean isTerminalStatus() {
        return status == NotificationDeliveryStatus.SENT
                || status == NotificationDeliveryStatus.FAILED
                || status == NotificationDeliveryStatus.UNKNOWN
                || status == NotificationDeliveryStatus.PERMANENTLY_FAILED;
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
