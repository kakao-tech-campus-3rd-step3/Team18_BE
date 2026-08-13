package com.kakaotech.team18.backend_server.domain.notification.quota;

import com.kakaotech.team18.backend_server.domain.notification.entity.NotificationQuotaBucket;
import com.kakaotech.team18.backend_server.domain.notification.repository.NotificationQuotaBucketRepository;
import com.kakaotech.team18.backend_server.domain.notification.sms.PreparedSmsMessage;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationChannel;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationQuotaPeriod;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class SolapiUsageQuotaService implements SolapiSendQuota {

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    private final NotificationQuotaBucketRepository repository;
    private final int hourlyLimit;
    private final int dailyLimit;
    private final int monthlyLimit;
    private final BigDecimal monthlyCostLimit;
    private final Clock clock;

    @Autowired
    public SolapiUsageQuotaService(
            NotificationQuotaBucketRepository repository,
            @Value("${notification.result.max-solapi-calls-per-hour:50}") int hourlyLimit,
            @Value("${notification.result.max-solapi-calls-per-day:50}") int dailyLimit,
            @Value("${notification.result.max-solapi-calls-per-month:1500}") int monthlyLimit,
            @Value("${notification.result.max-solapi-estimated-cost-per-month:0}") BigDecimal monthlyCostLimit
    ) {
        this(
                repository,
                hourlyLimit,
                dailyLimit,
                monthlyLimit,
                monthlyCostLimit,
                Clock.system(SERVICE_ZONE)
        );
    }

    SolapiUsageQuotaService(
            NotificationQuotaBucketRepository repository,
            int hourlyLimit,
            int dailyLimit,
            int monthlyLimit,
            BigDecimal monthlyCostLimit,
            Clock clock
    ) {
        if (hourlyLimit <= 0 || dailyLimit <= 0 || monthlyLimit <= 0
                || monthlyCostLimit.signum() < 0) {
            throw new IllegalArgumentException("SOLAPI 사용량 한도 설정값이 올바르지 않습니다.");
        }
        this.repository = repository;
        this.hourlyLimit = hourlyLimit;
        this.dailyLimit = dailyLimit;
        this.monthlyLimit = monthlyLimit;
        this.monthlyCostLimit = monthlyCostLimit;
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
    public void reserve(PreparedSmsMessage message) {
        LocalDateTime now = LocalDateTime.now(clock);
        reservePeriod(NotificationQuotaPeriod.HOUR, hourStart(now), hourlyLimit, BigDecimal.ZERO, message);
        reservePeriod(NotificationQuotaPeriod.DAY, dayStart(now), dailyLimit, BigDecimal.ZERO, message);
        reservePeriod(NotificationQuotaPeriod.MONTH, monthStart(now), monthlyLimit, monthlyCostLimit, message);
    }

    private void reservePeriod(
            NotificationQuotaPeriod period,
            LocalDateTime startedAt,
            int countLimit,
            BigDecimal costLimit,
            PreparedSmsMessage message
    ) {
        NotificationQuotaBucket bucket = repository
                .findByChannelAndPeriodAndBucketStartedAt(NotificationChannel.SMS, period, startedAt)
                .orElseGet(() -> NotificationQuotaBucket.startedAt(
                        NotificationChannel.SMS,
                        period,
                        startedAt
                ));
        if (!bucket.tryReserve(countLimit, costLimit, message.type(), message.estimatedCost())) {
            throw exceeded(period, startedAt);
        }
        int alertPercent = bucket.claimReachedAlertPercent(countLimit);
        repository.saveAndFlush(bucket);
        if (alertPercent > 0) {
            log.error(
                    "SOLAPI usage threshold reached: period={} percent={} count={} limit={} sms={} lms={} estimatedCost={}",
                    period,
                    alertPercent,
                    bucket.getRequestCount(),
                    countLimit,
                    bucket.getSmsCount(),
                    bucket.getLmsCount(),
                    bucket.getEstimatedCost()
            );
        }
    }

    private SolapiQuotaExceededException exceeded(
            NotificationQuotaPeriod period,
            LocalDateTime startedAt
    ) {
        return new SolapiQuotaExceededException(
                errorCode(period),
                "SOLAPI " + period + " 사용량 한도에 도달했습니다.",
                nextStart(period, startedAt)
        );
    }

    private String errorCode(NotificationQuotaPeriod period) {
        return switch (period) {
            case HOUR -> "SOLAPI_HOURLY_QUOTA_EXCEEDED";
            case DAY -> "SOLAPI_DAILY_QUOTA_EXCEEDED";
            case MONTH -> "SOLAPI_MONTHLY_QUOTA_EXCEEDED";
        };
    }

    private LocalDateTime hourStart(LocalDateTime now) {
        return now.withMinute(0).withSecond(0).withNano(0);
    }

    private LocalDateTime dayStart(LocalDateTime now) {
        return now.toLocalDate().atStartOfDay();
    }

    private LocalDateTime monthStart(LocalDateTime now) {
        return now.withDayOfMonth(1).toLocalDate().atStartOfDay();
    }

    private LocalDateTime nextStart(NotificationQuotaPeriod period, LocalDateTime startedAt) {
        return switch (period) {
            case HOUR -> startedAt.plusHours(1);
            case DAY -> startedAt.plusDays(1);
            case MONTH -> startedAt.plusMonths(1);
        };
    }
}
