package com.kakaotech.team18.backend_server.domain.notification.repository;

import com.kakaotech.team18.backend_server.domain.notification.entity.NotificationDelivery;
import com.kakaotech.team18.backend_server.domain.notification.entity.ResultNotificationRequest;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationChannel;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationDeliveryStatus;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationResultType;
import com.kakaotech.team18.backend_server.global.config.JpaAuditingConfig;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class NotificationDeliveryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NotificationDeliveryRepository repository;

    @Autowired
    private ResultNotificationRequestRepository requestRepository;

    @Test
    @DisplayName("알림 발송 작업과 수신자·메시지·공급자 정보를 저장한다")
    void saveDeliverySnapshot() {
        LocalDateTime nextAttemptAt = LocalDateTime.of(2026, 8, 3, 10, 0);
        NotificationDelivery delivery = NotificationDelivery.pending(
                1L,
                2L,
                3L,
                "idempotency-key",
                NotificationChannel.SMS,
                NotificationResultType.FINAL_REJECTED,
                "01012345678",
                null,
                null,
                "최종 결과 안내",
                nextAttemptAt
        );

        NotificationDelivery saved = repository.saveAndFlush(delivery);
        entityManager.clear();

        NotificationDelivery found = repository.findById(saved.getId()).orElseThrow();
        assertThat(found.getChannel()).isEqualTo(NotificationChannel.SMS);
        assertThat(found.getIdempotencyKey()).isEqualTo("idempotency-key");
        assertThat(found.getResultType()).isEqualTo(NotificationResultType.FINAL_REJECTED);
        assertThat(found.getStatus()).isEqualTo(NotificationDeliveryStatus.PENDING);
        assertThat(found.getRecipientAddress()).isEqualTo("01012345678");
        assertThat(found.getMessageBody()).isEqualTo("최종 결과 안내");
        assertThat(found.getAttemptCount()).isZero();
        assertThat(found.getNextAttemptAt()).isEqualTo(nextAttemptAt);
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getLastModifiedAt()).isNotNull();
    }

    @Test
    @DisplayName("동일 동아리에서는 같은 Idempotency-Key를 한 번만 저장한다")
    void preventDuplicateResultRequestKeyWithinClub() {
        ResultNotificationRequest first = ResultNotificationRequest.processing(
                1L,
                "duplicate-key",
                "a".repeat(64),
                com.kakaotech.team18.backend_server.domain.application.entity.Stage.INTERVIEW
        );
        ResultNotificationRequest duplicate = ResultNotificationRequest.processing(
                1L,
                "duplicate-key",
                "b".repeat(64),
                com.kakaotech.team18.backend_server.domain.application.entity.Stage.FINAL
        );

        requestRepository.saveAndFlush(first);

        assertThatThrownBy(() -> requestRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("동일 키·사용자·채널·결과 유형의 발송 작업은 중복 저장하지 않는다")
    void preventDuplicateDeliveryForRecipient() {
        LocalDateTime nextAttemptAt = LocalDateTime.of(2026, 8, 3, 10, 0);
        NotificationDelivery first = createDelivery(3L, nextAttemptAt);
        NotificationDelivery duplicate = createDelivery(4L, nextAttemptAt);

        repository.saveAndFlush(first);

        assertThatThrownBy(() -> repository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("재시도 시각이 지난 PENDING 작업만 조회한다")
    void findDuePendingDeliveries() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 12, 13, 0);
        NotificationDelivery due = createDeliveryWithKey("due-key", 10L, now.minusMinutes(1));
        NotificationDelivery future = createDeliveryWithKey("future-key", 11L, now.plusMinutes(1));
        repository.saveAllAndFlush(java.util.List.of(due, future));

        assertThat(repository.findDueDeliveryIds(now, PageRequest.of(0, 10)))
                .containsExactly(due.getId());
    }

    @Test
    @DisplayName("제한 시간을 넘긴 SENDING 작업만 복구 대상으로 조회한다")
    void findStaleSendingDeliveries() {
        LocalDateTime cutoff = LocalDateTime.of(2026, 8, 12, 13, 0);
        NotificationDelivery stale = createDeliveryWithKey("stale-key", 20L, cutoff.minusHours(1));
        stale.startSending(cutoff.minusMinutes(1));
        NotificationDelivery recent = createDeliveryWithKey("recent-key", 21L, cutoff.minusHours(1));
        recent.startSending(cutoff.plusMinutes(1));
        repository.saveAllAndFlush(java.util.List.of(stale, recent));

        assertThat(repository.findStaleSendingDeliveryIds(cutoff, PageRequest.of(0, 10)))
                .containsExactly(stale.getId());
    }

    @Test
    @DisplayName("상태 확인 시각이 지난 ACCEPTED 작업만 조회한다")
    void findDueAcceptedDeliveries() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 12, 13, 0);
        NotificationDelivery due = createDeliveryWithKey("accepted-due", 30L, now.minusMinutes(1));
        due.startSending(now.minusMinutes(2));
        due.markAccepted("group-1", "message-1", "2000", now.minusMinutes(2), now.minusMinutes(1));
        NotificationDelivery future = createDeliveryWithKey("accepted-future", 31L, now.minusMinutes(1));
        future.startSending(now.minusMinutes(2));
        future.markAccepted("group-2", "message-2", "2000", now.minusMinutes(2), now.plusMinutes(1));
        repository.saveAllAndFlush(java.util.List.of(due, future));

        assertThat(repository.findDueAcceptedDeliveryIds(now, PageRequest.of(0, 10)))
                .containsExactly(due.getId());
    }

    @Test
    @DisplayName("아직 개발자에게 보고하지 않은 최종 실패만 조회한다")
    void findUnalertedFailures() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 12, 13, 0);
        NotificationDelivery failure = createDeliveryWithKey("failed", 40L, now);
        failure.markPermanentlyFailed("INVALID_RECIPIENT", "invalid", now);
        NotificationDelivery alerted = createDeliveryWithKey("alerted", 41L, now);
        alerted.markPermanentlyFailed("INVALID_RECIPIENT", "invalid", now);
        alerted.markFailureAlerted(now);
        NotificationDelivery pending = createDeliveryWithKey("pending", 42L, now);
        repository.saveAllAndFlush(java.util.List.of(failure, alerted, pending));

        assertThat(repository.findUnalertedFailureIds(
                java.util.List.of(
                        NotificationDeliveryStatus.FAILED,
                        NotificationDeliveryStatus.UNKNOWN,
                        NotificationDeliveryStatus.PERMANENTLY_FAILED
                ),
                PageRequest.of(0, 10)
        )).containsExactly(failure.getId());
        assertThat(repository.countByStatus(NotificationDeliveryStatus.PERMANENTLY_FAILED))
                .isEqualTo(2);
    }

    @Test
    @DisplayName("동아리의 결과 발표 요청별 발송 상태 건수를 집계한다")
    void summarizeDeliveriesByResultRequest() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 13, 13, 0);
        ResultNotificationRequest request = ResultNotificationRequest.processing(
                1L,
                "summary-key",
                "a".repeat(64),
                com.kakaotech.team18.backend_server.domain.application.entity.Stage.FINAL
        );
        request.complete(true);
        requestRepository.saveAndFlush(request);

        NotificationDelivery pending = createDeliveryWithKey("summary-key", 51L, now);
        NotificationDelivery accepted = createDeliveryWithKey("summary-key", 52L, now);
        accepted.startSending(now);
        accepted.markAccepted("group-52", "message-52", "2000", now, now);
        NotificationDelivery sent = createDeliveryWithKey("summary-key", 53L, now);
        sent.startSending(now);
        sent.markSent("SMTP_ACCEPTED", now);
        NotificationDelivery failed = createDeliveryWithKey("summary-key", 54L, now);
        failed.startSending(now);
        failed.markAccepted("group-54", "message-54", "2000", now, now);
        failed.markFailed("5000", "CARRIER_FAILED", "failed", now);
        NotificationDelivery unknown = createDeliveryWithKey("summary-key", 55L, now);
        unknown.startSending(now);
        unknown.markUnknown("TIMEOUT", "unknown", now);
        repository.saveAllAndFlush(java.util.List.of(pending, accepted, sent, failed, unknown));
        entityManager.clear();

        com.kakaotech.team18.backend_server.domain.notification.dto.ResultNotificationRequestSummary summary =
                requestRepository.findSummariesByClubId(1L, PageRequest.of(0, 10)).getFirst();

        assertThat(summary.requestId()).isEqualTo(request.getId());
        assertThat(summary.total()).isEqualTo(5);
        assertThat(summary.pending()).isEqualTo(1);
        assertThat(summary.accepted()).isEqualTo(1);
        assertThat(summary.sent()).isEqualTo(1);
        assertThat(summary.failed()).isEqualTo(1);
        assertThat(summary.unknown()).isEqualTo(1);
    }

    @Test
    @DisplayName("생성된 지 오래됐고 아직 보고하지 않은 PENDING 작업만 조회한다")
    void findUnalertedLongPendingDeliveries() {
        LocalDateTime now = LocalDateTime.now();
        NotificationDelivery oldPending = createDeliveryWithKey("old-pending", 61L, now);
        NotificationDelivery alerted = createDeliveryWithKey("alerted-pending", 62L, now);
        alerted.markPendingAlerted(now);
        repository.saveAllAndFlush(java.util.List.of(oldPending, alerted));
        entityManager.clear();

        assertThat(repository.findUnalertedLongPendingIds(
                now.plusMinutes(1),
                PageRequest.of(0, 10)
        )).containsExactly(oldPending.getId());
    }

    private NotificationDelivery createDelivery(Long applicationId, LocalDateTime nextAttemptAt) {
        return NotificationDelivery.pending(
                1L,
                2L,
                applicationId,
                "same-delivery-key",
                NotificationChannel.SMS,
                NotificationResultType.FINAL_REJECTED,
                "01012345678",
                null,
                null,
                "최종 결과 안내",
                nextAttemptAt
        );
    }

    private NotificationDelivery createDeliveryWithKey(
            String idempotencyKey,
            Long applicationId,
            LocalDateTime nextAttemptAt
    ) {
        return NotificationDelivery.pending(
                1L,
                applicationId,
                applicationId,
                idempotencyKey,
                NotificationChannel.EMAIL,
                NotificationResultType.FINAL_APPROVED,
                "applicant" + applicationId + "@example.com",
                "president@example.com",
                "결과 안내",
                "합격을 축하드립니다.",
                nextAttemptAt
        );
    }
}
