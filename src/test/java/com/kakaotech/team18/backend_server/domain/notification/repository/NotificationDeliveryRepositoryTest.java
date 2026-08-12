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
