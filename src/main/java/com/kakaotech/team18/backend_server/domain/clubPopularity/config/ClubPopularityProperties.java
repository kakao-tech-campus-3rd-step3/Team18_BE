package com.kakaotech.team18.backend_server.domain.clubPopularity.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "club-popularity")
public class ClubPopularityProperties {

    private boolean enabled = true;

    // 인기 배지 판정 기준
    @Min(1)
    private int recentViewerThreshold = 10;

    @Min(1)
    private int activeViewerThreshold = 3;

    @Min(1)
    private int heartbeatIntervalSeconds = 30;

    @Min(1)
    private int activeTtlSeconds = 180;

    // Redis/DB 배치의 보관·처리 상한
    @Min(25)
    private int retentionHours = 25;

    @Min(1)
    private int flushIntervalMinutes = 60;

    @Min(1)
    private int flushBatchSize = 500;

    @Min(1)
    private int cleanupBatchSize = 500;

    @Min(1)
    private int flushMaxRecordsPerRun = 10_000;

    @Min(1)
    private int flushMaxDbAttemptsPerRun = 32;

    @Min(1)
    private int flushMaxDurationMinutes = 15;

    // 일시 장애와 문제 데이터의 재시도 일정
    @Valid
    @NotEmpty
    private List<@Positive Integer> transientRetryDelaysSeconds = new ArrayList<>(List.of(1, 3, 10));

    @Valid
    @NotEmpty
    private List<@Positive Integer> failedRecordRetryDelaysMinutes = new ArrayList<>(List.of(10, 60, 360));

    @Min(1)
    private int failedRecordMaxAttempts = 3;

    @Min(25)
    private int failedRecordRetentionHours = 25;

    // 요청 제한과 Redis 복구 주기
    @Min(0)
    @Max(20)
    private int retryJitterPercent = 20;

    @Min(1)
    private int viewMinIntervalSeconds = 5;

    @Min(1)
    private int heartbeatMinIntervalSeconds = 20;

    @Min(1)
    private int recoveryCheckIntervalMinutes = 5;

    @Min(1)
    private int recoveryMaxDurationMinutes = 30;

    @AssertTrue(message = "active-ttl-seconds must be greater than heartbeat-interval-seconds")
    public boolean isActiveTtlLongerThanHeartbeatInterval() {
        return activeTtlSeconds > heartbeatIntervalSeconds;
    }

    @AssertTrue(message = "heartbeat-min-interval-seconds must be less than heartbeat-interval-seconds")
    public boolean isHeartbeatRateLimitShorterThanHeartbeatInterval() {
        return heartbeatMinIntervalSeconds < heartbeatIntervalSeconds;
    }

    @AssertTrue(message = "flush-batch-size and cleanup-batch-size must not exceed flush-max-records-per-run")
    public boolean areBatchSizesWithinRunLimit() {
        return flushBatchSize <= flushMaxRecordsPerRun && cleanupBatchSize <= flushMaxRecordsPerRun;
    }

    @AssertTrue(message = "failed-record-max-attempts must match the number of failed-record retry delays")
    public boolean isFailedRetryScheduleComplete() {
        return failedRecordRetryDelaysMinutes != null
                && failedRecordMaxAttempts == failedRecordRetryDelaysMinutes.size();
    }
}
