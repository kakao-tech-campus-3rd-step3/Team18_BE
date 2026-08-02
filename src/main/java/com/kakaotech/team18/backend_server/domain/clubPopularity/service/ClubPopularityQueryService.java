package com.kakaotech.team18.backend_server.domain.clubPopularity.service;

import com.kakaotech.team18.backend_server.domain.clubPopularity.config.ClubPopularityProperties;
import com.kakaotech.team18.backend_server.domain.clubPopularity.model.ClubPopularityTimePolicy;
import com.kakaotech.team18.backend_server.domain.clubPopularity.redis.ClubPopularityRedisRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

    public ClubPopularityResponse getPopularClubs() {
        if (!properties.isEnabled()) {
            return ClubPopularityResponse.empty();
        }
        try {
            if (!READY.equals(redisRepository.recoveryStatus())) {
                return ClubPopularityResponse.empty();
            }

            Instant now = timePolicy.currentInstant();
            long nowMillis = now.toEpochMilli();
            long recentCutoffMillis = now.minusSeconds(24 * 60 * 60).toEpochMilli();
            long activeCutoffMillis = now.minusSeconds(properties.getActiveTtlSeconds()).toEpochMilli();
            Set<String> candidates = redisRepository.candidateClubIds();
            List<PopularClubResponse> popularClubs = new ArrayList<>();

            for (String candidate : candidates) {
                long clubId;
                try {
                    clubId = Long.parseLong(candidate);
                } catch (NumberFormatException exception) {
                    log.warn("Ignoring malformed club popularity candidate: {}", candidate);
                    continue;
                }
                ClubPopularityRedisRepository.ViewerCounts counts = redisRepository.aggregate(
                        clubId, nowMillis, recentCutoffMillis, activeCutoffMillis);
                boolean recentBadge = counts.recentViewerCount() >= properties.getRecentViewerThreshold();
                boolean activeBadge = counts.activeViewerCount() >= properties.getActiveViewerThreshold();
                if (recentBadge || activeBadge) {
                    popularClubs.add(new PopularClubResponse(
                            clubId, counts.recentViewerCount(), counts.activeViewerCount(), recentBadge, activeBadge));
                }
            }

            // 집계 중 복구로 전환되면 부분 결과를 노출하지 않는다.
            if (!READY.equals(redisRepository.recoveryStatus())) {
                return ClubPopularityResponse.empty();
            }
            return new ClubPopularityResponse(popularClubs);
        } catch (DataAccessException exception) {
            log.warn("Club popularity query failed: {}", exception.getMessage());
            return ClubPopularityResponse.empty();
        }
    }

}
