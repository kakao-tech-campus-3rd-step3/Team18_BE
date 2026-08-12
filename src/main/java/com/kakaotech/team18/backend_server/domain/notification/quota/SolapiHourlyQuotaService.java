package com.kakaotech.team18.backend_server.domain.notification.quota;

import com.kakaotech.team18.backend_server.domain.notification.entity.NotificationQuotaBucket;
import com.kakaotech.team18.backend_server.domain.notification.repository.NotificationQuotaBucketRepository;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationChannel;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SolapiHourlyQuotaService implements SolapiSendQuota {

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    private final NotificationQuotaBucketRepository repository;
    private final int hourlyLimit;
    private final Clock clock;

    @Autowired
    public SolapiHourlyQuotaService(
            NotificationQuotaBucketRepository repository,
            @Value("${notification.result.max-solapi-calls-per-hour:100}") int hourlyLimit
    ) {
        this(repository, hourlyLimit, Clock.system(SERVICE_ZONE));
    }

    SolapiHourlyQuotaService(
            NotificationQuotaBucketRepository repository,
            int hourlyLimit,
            Clock clock
    ) {
        if (hourlyLimit <= 0) {
            throw new IllegalArgumentException("SOLAPI 시간당 호출 한도는 1 이상이어야 합니다.");
        }
        this.repository = repository;
        this.hourlyLimit = hourlyLimit;
        this.clock = clock;
    }

    @Override
    @Retryable(
            retryFor = {
                    ObjectOptimisticLockingFailureException.class,
                    DataIntegrityViolationException.class
            },
            maxAttempts = 5,
            backoff = @Backoff(delay = 20, multiplier = 2)
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reserve() {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime bucketStartedAt = now.withMinute(0).withSecond(0).withNano(0);
        NotificationQuotaBucket bucket = repository
                .findByChannelAndBucketStartedAt(NotificationChannel.SMS, bucketStartedAt)
                .orElseGet(() -> NotificationQuotaBucket.hourly(
                        NotificationChannel.SMS,
                        bucketStartedAt
                ));

        if (!bucket.tryReserve(hourlyLimit)) {
            throw new SolapiQuotaExceededException(bucketStartedAt.plusHours(1));
        }
        repository.saveAndFlush(bucket);
    }
}
