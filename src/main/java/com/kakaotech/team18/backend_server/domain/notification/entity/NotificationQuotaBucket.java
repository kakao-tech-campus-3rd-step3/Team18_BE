package com.kakaotech.team18.backend_server.domain.notification.entity;

import com.kakaotech.team18.backend_server.domain.BaseEntity;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationChannel;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationQuotaPeriod;
import com.kakaotech.team18.backend_server.domain.notification.sms.SmsMessageType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "notification_quota_bucket",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_notification_quota_channel_period_start",
                columnNames = {"channel", "period", "bucket_started_at"}
        )
)
public class NotificationQuotaBucket extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_quota_bucket_id")
    private Long id;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "period", nullable = false, length = 20)
    private NotificationQuotaPeriod period;

    @Column(name = "bucket_started_at", nullable = false)
    private LocalDateTime bucketStartedAt;

    @Column(name = "request_count", nullable = false)
    private int requestCount;

    @Column(name = "sms_count", nullable = false)
    private int smsCount;

    @Column(name = "lms_count", nullable = false)
    private int lmsCount;

    @Column(name = "estimated_cost", nullable = false, precision = 14, scale = 4)
    private BigDecimal estimatedCost;

    @Column(name = "highest_alerted_percent", nullable = false)
    private int highestAlertedPercent;

    private NotificationQuotaBucket(
            NotificationChannel channel,
            NotificationQuotaPeriod period,
            LocalDateTime bucketStartedAt
    ) {
        this.channel = channel;
        this.period = period;
        this.bucketStartedAt = bucketStartedAt;
        this.requestCount = 0;
        this.smsCount = 0;
        this.lmsCount = 0;
        this.estimatedCost = BigDecimal.ZERO;
        this.highestAlertedPercent = 0;
    }

    public static NotificationQuotaBucket startedAt(
            NotificationChannel channel,
            NotificationQuotaPeriod period,
            LocalDateTime bucketStartedAt
    ) {
        return new NotificationQuotaBucket(channel, period, bucketStartedAt);
    }

    public boolean tryReserve(
            int countLimit,
            BigDecimal costLimit,
            SmsMessageType messageType,
            BigDecimal messageCost
    ) {
        if (requestCount >= countLimit
                || isCostLimitExceeded(costLimit, messageCost)) {
            return false;
        }
        requestCount++;
        if (messageType == SmsMessageType.SMS) {
            smsCount++;
        } else {
            lmsCount++;
        }
        estimatedCost = estimatedCost.add(messageCost);
        return true;
    }

    public int claimReachedAlertPercent(int limit) {
        int usagePercent = requestCount * 100 / limit;
        int reached = usagePercent >= 100 ? 100 : usagePercent >= 90 ? 90 : usagePercent >= 70 ? 70 : 0;
        if (reached <= highestAlertedPercent) {
            return 0;
        }
        highestAlertedPercent = reached;
        return reached;
    }

    private boolean isCostLimitExceeded(BigDecimal costLimit, BigDecimal messageCost) {
        return costLimit.signum() > 0
                && estimatedCost.add(messageCost).compareTo(costLimit) > 0;
    }
}
