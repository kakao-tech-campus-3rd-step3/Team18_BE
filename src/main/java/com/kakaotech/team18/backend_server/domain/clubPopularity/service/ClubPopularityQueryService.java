package com.kakaotech.team18.backend_server.domain.clubPopularity.service;

import com.kakaotech.team18.backend_server.domain.clubPopularity.config.ClubPopularityProperties;
import com.kakaotech.team18.backend_server.domain.clubPopularity.model.ClubPopularityTimePolicy;
import com.kakaotech.team18.backend_server.domain.clubPopularity.redis.ClubPopularityRedisRepository;
import com.kakaotech.team18.backend_server.domain.clubPopularity.metrics.ClubPopularityMetrics;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import com.kakaotech.team18.backend_server.domain.clubPopularity.dto.ClubPopularityResponse;
import com.kakaotech.team18.backend_server.domain.clubPopularity.dto.PopularClubResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClubPopularityQueryService {

    private static final String READY = "READY";

    private final ClubPopularityProperties properties;
    private final ClubPopularityTimePolicy timePolicy;
    private final ClubPopularityRedisRepository redisRepository;
    private final ClubPopularityMetrics metrics;

    public ClubPopularityResponse getPopularClubs() {
        if (!properties.isEnabled()) {
            metrics.setEnabled(false);
            metrics.recordApi("popular", "disabled");
            return ClubPopularityResponse.empty();
        }
        try {
            if (!READY.equals(redisRepository.recoveryStatus())) {
                metrics.recordApi("popular", "recovering");
                return ClubPopularityResponse.empty();
            }

            Instant now = timePolicy.currentInstant();
            long nowMillis = now.toEpochMilli();
            long recentCutoffMillis = now.minusSeconds((long) properties.getRecentViewerWindowHours() * 60 * 60)
                    .toEpochMilli();
            long activeCutoffMillis = now.minusSeconds(properties.getActiveTtlSeconds()).toEpochMilli();
            Set<String> candidates = redisRepository.candidateClubIds();
            List<PopularClubResponse> popularClubs = new ArrayList<>();
            Set<Long> validCandidateIds = new LinkedHashSet<>();

            for (String candidate : candidates) {
                try {
                    validCandidateIds.add(Long.parseLong(candidate));
                } catch (NumberFormatException exception) {
                    log.warn("Ignoring malformed club popularity candidate: {}", candidate);
                }
            }
            Map<Long, ClubPopularityRedisRepository.ViewerCounts> aggregated = redisRepository.aggregateAll(
                    validCandidateIds, nowMillis, recentCutoffMillis, activeCutoffMillis);
            for (Map.Entry<Long, ClubPopularityRedisRepository.ViewerCounts> entry : aggregated.entrySet()) {
                long clubId = entry.getKey();
                ClubPopularityRedisRepository.ViewerCounts counts = entry.getValue();
                boolean recentBadge = counts.recentViewerCount() >= properties.getRecentViewerThreshold();
                boolean activeBadge = counts.activeViewerCount() >= properties.getActiveViewerThreshold();
                if (recentBadge || activeBadge) {
                    popularClubs.add(new PopularClubResponse(
                            clubId, counts.recentViewerCount(), counts.activeViewerCount(), recentBadge, activeBadge));
                }
            }

            // 집계 중 복구로 전환되면 부분 결과를 노출하지 않는다.
            if (!READY.equals(redisRepository.recoveryStatus())) {
                metrics.recordApi("popular", "recovering");
                return ClubPopularityResponse.empty();
            }
            metrics.recordApi("popular", "success");
            return new ClubPopularityResponse(popularClubs);
        } catch (DataAccessException exception) {
            metrics.recordRedisError("popular");
            metrics.recordApi("popular", "redis_error");
            log.warn("Club popularity query failed: {}", exception.getMessage());
            return ClubPopularityResponse.empty();
        }
    }

}
