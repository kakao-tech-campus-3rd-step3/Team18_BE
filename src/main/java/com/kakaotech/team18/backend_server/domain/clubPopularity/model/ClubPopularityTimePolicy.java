package com.kakaotech.team18.backend_server.domain.clubPopularity.model;

import com.kakaotech.team18.backend_server.domain.clubPopularity.config.ClubPopularityProperties;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClubPopularityTimePolicy {

    private static final Duration RECENT_VIEW_WINDOW = Duration.ofHours(24);

    private final ClubPopularityProperties properties;
    private final Clock clubPopularityClock;

    public Instant currentInstant() {
        return clubPopularityClock.instant();
    }

    public boolean isRecentViewer(Instant lastViewedAt, Instant now) {
        // 경계 시각은 제외: cutoff < lastViewedAt <= now
        Instant cutoff = now.minus(RECENT_VIEW_WINDOW);
        return lastViewedAt.isAfter(cutoff) && !lastViewedAt.isAfter(now);
    }

    public boolean isActiveViewer(Instant lastActiveAt, Instant now) {
        // heartbeat TTL도 최근 조회와 같은 반열린 구간을 사용한다.
        Instant cutoff = now.minusSeconds(properties.getActiveTtlSeconds());
        return lastActiveAt.isAfter(cutoff) && !lastActiveAt.isAfter(now);
    }

    public boolean shouldDeleteViewRecord(Instant lastViewedAt, Instant now) {
        // 보관 기한에 도달한 기록은 즉시 정리 대상이다.
        Instant cutoff = now.minus(Duration.ofHours(properties.getRetentionHours()));
        return !lastViewedAt.isAfter(cutoff);
    }
}
