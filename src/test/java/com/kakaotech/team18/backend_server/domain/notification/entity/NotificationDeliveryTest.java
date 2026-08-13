package com.kakaotech.team18.backend_server.domain.notification.entity;

import com.kakaotech.team18.backend_server.domain.notification.type.NotificationChannel;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationDeliveryStatus;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationResultType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationDeliveryTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 8, 3, 10, 0);

    @Test
    @DisplayName("새 발송 작업은 PENDING 상태와 시도 횟수 0으로 생성된다")
    void createPendingDelivery() {
        NotificationDelivery delivery = createDelivery();

        assertThat(delivery.getStatus()).isEqualTo(NotificationDeliveryStatus.PENDING);
        assertThat(delivery.getAttemptCount()).isZero();
        assertThat(delivery.getNextAttemptAt()).isEqualTo(CREATED_AT);
    }

    @Test
    @DisplayName("SOLAPI 접수와 웹훅 성공 상태를 순서대로 기록한다")
    void acceptedThenSent() {
        NotificationDelivery delivery = createDelivery();
        LocalDateTime attemptedAt = CREATED_AT.plusMinutes(1);
        LocalDateTime acceptedAt = CREATED_AT.plusMinutes(2);
        LocalDateTime sentAt = CREATED_AT.plusMinutes(3);

        delivery.startSending(attemptedAt);
        delivery.markAccepted("group-id", "message-id", "2000", acceptedAt, acceptedAt);
        delivery.markSent("4000", sentAt);

        assertThat(delivery.getStatus()).isEqualTo(NotificationDeliveryStatus.SENT);
        assertThat(delivery.getAttemptCount()).isEqualTo(1);
        assertThat(delivery.getLastAttemptAt()).isEqualTo(attemptedAt);
        assertThat(delivery.getAcceptedAt()).isEqualTo(acceptedAt);
        assertThat(delivery.getSentAt()).isEqualTo(sentAt);
        assertThat(delivery.getProviderGroupId()).isEqualTo("group-id");
        assertThat(delivery.getProviderMessageId()).isEqualTo("message-id");
        assertThat(delivery.getProviderStatusCode()).isEqualTo("4000");
    }

    @Test
    @DisplayName("일시적 실패는 다음 시도 시각과 함께 PENDING으로 되돌린다")
    void rescheduleRetryableFailure() {
        NotificationDelivery delivery = createDelivery();
        LocalDateTime retryAt = CREATED_AT.plusMinutes(10);

        delivery.startSending(CREATED_AT.plusMinutes(1));
        delivery.reschedule(retryAt, "HTTP_503", "SOLAPI temporary failure");

        assertThat(delivery.getStatus()).isEqualTo(NotificationDeliveryStatus.PENDING);
        assertThat(delivery.getNextAttemptAt()).isEqualTo(retryAt);
        assertThat(delivery.getProviderErrorCode()).isEqualTo("HTTP_503");
    }

    @Test
    @DisplayName("SOLAPI 접수 후 통신사 실패가 확정되면 FAILED로 기록한다")
    void acceptedThenFailed() {
        NotificationDelivery delivery = createDelivery();
        LocalDateTime failedAt = CREATED_AT.plusMinutes(3);

        delivery.startSending(CREATED_AT.plusMinutes(1));
        delivery.markAccepted(
                "group-id",
                "message-id",
                "2000",
                CREATED_AT.plusMinutes(2),
                CREATED_AT.plusMinutes(2)
        );
        delivery.markFailed("5000", "CARRIER_REJECTED", "carrier rejected", failedAt);

        assertThat(delivery.getStatus()).isEqualTo(NotificationDeliveryStatus.FAILED);
        assertThat(delivery.getFailedAt()).isEqualTo(failedAt);
        assertThat(delivery.getProviderStatusCode()).isEqualTo("5000");
    }

    @Test
    @DisplayName("접수 여부를 알 수 없는 타임아웃은 UNKNOWN으로 기록한다")
    void markUnknownOnAmbiguousTimeout() {
        NotificationDelivery delivery = createDelivery();
        LocalDateTime unknownAt = CREATED_AT.plusMinutes(2);

        delivery.startSending(CREATED_AT.plusMinutes(1));
        delivery.markUnknown("READ_TIMEOUT", "acceptance is unknown", unknownAt);

        assertThat(delivery.getStatus()).isEqualTo(NotificationDeliveryStatus.UNKNOWN);
        assertThat(delivery.getUnknownAt()).isEqualTo(unknownAt);
        assertThat(delivery.getFailedAt()).isNull();
    }

    @Test
    @DisplayName("재시도할 수 없는 오류는 PERMANENTLY_FAILED로 기록한다")
    void markPermanentlyFailed() {
        NotificationDelivery delivery = createDelivery();
        LocalDateTime failedAt = CREATED_AT.plusMinutes(1);

        delivery.markPermanentlyFailed("INVALID_RECIPIENT", "invalid phone number", failedAt);

        assertThat(delivery.getStatus()).isEqualTo(NotificationDeliveryStatus.PERMANENTLY_FAILED);
        assertThat(delivery.getFailedAt()).isEqualTo(failedAt);
    }

    @Test
    @DisplayName("최종 실패는 개발자 알림 완료 시각을 한 번만 기록한다")
    void markFailureAlertedOnce() {
        NotificationDelivery delivery = createDelivery();
        LocalDateTime alertedAt = CREATED_AT.plusMinutes(2);
        delivery.markPermanentlyFailed("INVALID_RECIPIENT", "invalid phone number", CREATED_AT);

        delivery.markFailureAlerted(alertedAt);

        assertThat(delivery.getFailureAlertedAt()).isEqualTo(alertedAt);
        assertThatThrownBy(() -> delivery.markFailureAlerted(alertedAt.plusMinutes(1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("진행 중인 작업은 실패 알림 완료로 표시할 수 없다")
    void cannotAlertNonFailure() {
        NotificationDelivery delivery = createDelivery();

        assertThatThrownBy(() -> delivery.markFailureAlerted(CREATED_AT))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("PENDING 작업은 장기 대기 알림 완료 시각을 한 번만 기록한다")
    void markPendingAlertedOnce() {
        NotificationDelivery delivery = createDelivery();

        delivery.markPendingAlerted(CREATED_AT);

        assertThat(delivery.getPendingAlertedAt()).isEqualTo(CREATED_AT);
        assertThatThrownBy(() -> delivery.markPendingAlerted(CREATED_AT.plusMinutes(1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("이메일은 별도 접수 상태 없이 SENDING에서 SENT로 완료할 수 있다")
    void emailCanBeSentWithoutAccepted() {
        NotificationDelivery delivery = NotificationDelivery.pending(
                1L,
                2L,
                3L,
                "idempotency-key",
                NotificationChannel.EMAIL,
                NotificationResultType.FINAL_APPROVED,
                "applicant@example.com",
                "president@example.com",
                "최종 합격 안내",
                "최종 합격을 축하드립니다.",
                CREATED_AT
        );
        LocalDateTime sentAt = CREATED_AT.plusMinutes(2);

        delivery.startSending(CREATED_AT.plusMinutes(1));
        delivery.markSent("SMTP_ACCEPTED", sentAt);

        assertThat(delivery.getStatus()).isEqualTo(NotificationDeliveryStatus.SENT);
        assertThat(delivery.getAcceptedAt()).isNull();
        assertThat(delivery.getSentAt()).isEqualTo(sentAt);
    }

    @Test
    @DisplayName("ACCEPTED 이전에는 통신사 최종 실패로 변경할 수 없다")
    void rejectInvalidFailedTransition() {
        NotificationDelivery delivery = createDelivery();
        delivery.startSending(CREATED_AT.plusMinutes(1));

        assertThatThrownBy(() -> delivery.markFailed(
                "5000",
                "CARRIER_REJECTED",
                "carrier rejected",
                CREATED_AT.plusMinutes(2)
        )).isInstanceOf(IllegalStateException.class);
    }

    private NotificationDelivery createDelivery() {
        return NotificationDelivery.pending(
                1L,
                2L,
                3L,
                "idempotency-key",
                NotificationChannel.SMS,
                NotificationResultType.INTERVIEW_APPROVED,
                "01012345678",
                null,
                null,
                "면접 합격 안내",
                CREATED_AT
        );
    }
}
