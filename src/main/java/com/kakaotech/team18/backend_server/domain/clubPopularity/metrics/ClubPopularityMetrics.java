package com.kakaotech.team18.backend_server.domain.clubPopularity.metrics;

import com.kakaotech.team18.backend_server.domain.clubPopularity.config.ClubPopularityProperties;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

/** 인기 기능의 관찰 지점을 한 곳에서 관리한다. 식별자 원문은 태그로 사용하지 않는다. */
@Component
@RequiredArgsConstructor
public class ClubPopularityMetrics {

    private final MeterRegistry registry;
    private final ClubPopularityProperties properties;
    private final AtomicLong pendingCount = new AtomicLong();
    private final AtomicLong oldestPendingAgeSeconds = new AtomicLong();
    private final AtomicLong failedCount = new AtomicLong();
    private final AtomicLong recoveryDurationSeconds = new AtomicLong();
    private final AtomicBoolean recoveryInProgress = new AtomicBoolean();
    private final AtomicBoolean enabled = new AtomicBoolean(true);

    public void recordApi(String api, String result) {
        Counter.builder("club.popularity.api.requests")
                .tag("api", api).tag("result", result).register(registry).increment();
    }

    public void recordRedisError(String operation) {
        Counter.builder("club.popularity.redis.errors")
                .tag("operation", operation).register(registry).increment();
    }

    public void recordDbSaved(long count) {
        registry.counter("club.popularity.db.saved").increment(count);
    }

    public void recordDbMissing(long count) {
        registry.counter("club.popularity.db.missing").increment(count);
    }

    public void recordDbFailed(long count) {
        registry.counter("club.popularity.db.failed").increment(count);
    }

    public Timer.Sample startDbTimer() {
        return Timer.start(registry);
    }

    public void stopDbTimer(Timer.Sample sample) {
        sample.stop(registry.timer("club.popularity.db.flush.duration"));
    }

    public void setPendingState(long count, long oldestAgeSeconds) {
        pendingCount.set(count);
        oldestPendingAgeSeconds.set(oldestAgeSeconds);
    }

    public void setFailedCount(long count) {
        failedCount.set(count);
    }

    public void setRecoveryInProgress(boolean value) {
        recoveryInProgress.set(value);
    }

    public void setEnabled(boolean value) {
        enabled.set(value);
    }

    public void recordRecovery(String result, long durationSeconds) {
        recoveryDurationSeconds.set(durationSeconds);
        Counter.builder("club.popularity.recovery.results").tag("result", result).register(registry).increment();
    }

    public void registerGauges() {
        Gauge.builder("club.popularity.pending.count", pendingCount, AtomicLong::get).register(registry);
        Gauge.builder("club.popularity.pending.oldest.age.seconds", oldestPendingAgeSeconds, AtomicLong::get)
                .register(registry);
        Gauge.builder("club.popularity.failed.count", failedCount, AtomicLong::get).register(registry);
        Gauge.builder("club.popularity.recovery.in.progress", recoveryInProgress, value -> value.get() ? 1 : 0)
                .register(registry);
        Gauge.builder("club.popularity.recovery.duration.seconds", recoveryDurationSeconds, AtomicLong::get)
                .register(registry);
        Gauge.builder("club.popularity.enabled", enabled, value -> value.get() ? 1 : 0).register(registry);
    }

    @PostConstruct
    void initialize() {
        enabled.set(properties.isEnabled());
        registerGauges();
    }
}
