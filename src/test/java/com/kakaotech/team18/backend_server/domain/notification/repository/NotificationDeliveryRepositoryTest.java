package com.kakaotech.team18.backend_server.domain.notification.repository;

import com.kakaotech.team18.backend_server.domain.notification.entity.NotificationDelivery;
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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class NotificationDeliveryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NotificationDeliveryRepository repository;

    @Test
    @DisplayName("알림 발송 작업과 수신자·메시지·공급자 정보를 저장한다")
    void saveDeliverySnapshot() {
        LocalDateTime nextAttemptAt = LocalDateTime.of(2026, 8, 3, 10, 0);
        NotificationDelivery delivery = NotificationDelivery.pending(
                1L,
                2L,
                3L,
                NotificationChannel.SMS,
                NotificationResultType.FINAL_REJECTED,
                "01012345678",
                null,
                "최종 결과 안내",
                nextAttemptAt
        );

        NotificationDelivery saved = repository.saveAndFlush(delivery);
        entityManager.clear();

        NotificationDelivery found = repository.findById(saved.getId()).orElseThrow();
        assertThat(found.getChannel()).isEqualTo(NotificationChannel.SMS);
        assertThat(found.getResultType()).isEqualTo(NotificationResultType.FINAL_REJECTED);
        assertThat(found.getStatus()).isEqualTo(NotificationDeliveryStatus.PENDING);
        assertThat(found.getRecipientAddress()).isEqualTo("01012345678");
        assertThat(found.getMessageBody()).isEqualTo("최종 결과 안내");
        assertThat(found.getAttemptCount()).isZero();
        assertThat(found.getNextAttemptAt()).isEqualTo(nextAttemptAt);
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getLastModifiedAt()).isNotNull();
    }
}
