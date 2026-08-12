package com.kakaotech.team18.backend_server.domain.notification.entity;

import com.kakaotech.team18.backend_server.domain.BaseEntity;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationChannel;
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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "notification_quota_bucket",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_notification_quota_channel_hour",
                columnNames = {"channel", "bucket_started_at"}
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

    @Column(name = "bucket_started_at", nullable = false)
    private LocalDateTime bucketStartedAt;

    @Column(name = "request_count", nullable = false)
    private int requestCount;

    private NotificationQuotaBucket(NotificationChannel channel, LocalDateTime bucketStartedAt) {
        this.channel = channel;
        this.bucketStartedAt = bucketStartedAt;
        this.requestCount = 0;
    }

    public static NotificationQuotaBucket hourly(
            NotificationChannel channel,
            LocalDateTime bucketStartedAt
    ) {
        return new NotificationQuotaBucket(channel, bucketStartedAt);
    }

    public boolean tryReserve(int limit) {
        if (requestCount >= limit) {
            return false;
        }
        requestCount++;
        return true;
    }
}
