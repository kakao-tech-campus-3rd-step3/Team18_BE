package com.kakaotech.team18.backend_server.domain.notification.repository;

import com.kakaotech.team18.backend_server.domain.notification.entity.NotificationQuotaBucket;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationChannel;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationQuotaPeriod;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationQuotaBucketRepository
        extends JpaRepository<NotificationQuotaBucket, Long> {

    Optional<NotificationQuotaBucket> findByChannelAndPeriodAndBucketStartedAt(
            NotificationChannel channel,
            NotificationQuotaPeriod period,
            LocalDateTime bucketStartedAt
    );
}
