package com.kakaotech.team18.backend_server.domain.clubPopularity.service;

import com.kakaotech.team18.backend_server.domain.clubPopularity.config.ClubPopularityProperties;
import com.kakaotech.team18.backend_server.domain.clubPopularity.model.ClubPopularityTimePolicy;
import com.kakaotech.team18.backend_server.domain.clubPopularity.model.ClubPopularityViewerIdentity;
import com.kakaotech.team18.backend_server.domain.clubPopularity.redis.ClubPopularityRedisRepository;
import com.kakaotech.team18.backend_server.domain.clubPopularity.metrics.ClubPopularityMetrics;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClubPopularityRecordingService {

    private final ClubPopularityProperties properties;
    private final ClubPopularityTimePolicy timePolicy;
    private final ClubPopularityViewerResolver viewerResolver;
    private final ClubPopularityRedisRepository redisRepository;
    private final ClubPopularityMetrics metrics;

    public RecordingResult recordView(long clubId, Authentication authentication, String anonymousId) {
        if (!properties.isEnabled()) {
            metrics.setEnabled(false);
            metrics.recordApi("views", "disabled");
            return RecordingResult.DISABLED;
        }
        Optional<ClubPopularityViewerIdentity> identity = viewerResolver.resolve(authentication, anonymousId);
        if (identity.isEmpty()) {
            metrics.recordApi("views", "invalid_identity");
            return RecordingResult.INVALID_IDENTITY;
        }
        try {
            long nowMillis = timePolicy.currentInstant().toEpochMilli();
            RecordingResult result = map(redisRepository.recordView(clubId, identity.get(), nowMillis,
                    properties.getViewMinIntervalSeconds(), properties.getActiveTtlSeconds(),
                    (int) properties.getRetentionHours() * 60 * 60));
            metrics.recordApi("views", result.name().toLowerCase());
            return result;
        } catch (DataAccessException exception) {
            metrics.recordRedisError("views");
            metrics.recordApi("views", "redis_error");
            log.warn("Club popularity view recording failed: {}", exception.getMessage());
            return RecordingResult.REDIS_ERROR;
        }
    }

    public RecordingResult recordHeartbeat(long clubId, Authentication authentication, String anonymousId) {
        if (!properties.isEnabled()) {
            metrics.setEnabled(false);
            metrics.recordApi("heartbeat", "disabled");
            return RecordingResult.DISABLED;
        }
        Optional<ClubPopularityViewerIdentity> identity = viewerResolver.resolve(authentication, anonymousId);
        if (identity.isEmpty()) {
            metrics.recordApi("heartbeat", "invalid_identity");
            return RecordingResult.INVALID_IDENTITY;
        }
        try {
            long nowMillis = timePolicy.currentInstant().toEpochMilli();
            RecordingResult result = map(redisRepository.recordHeartbeat(clubId, identity.get(), nowMillis,
                    properties.getHeartbeatMinIntervalSeconds(), properties.getActiveTtlSeconds()));
            metrics.recordApi("heartbeat", result.name().toLowerCase());
            return result;
        } catch (DataAccessException exception) {
            metrics.recordRedisError("heartbeat");
            metrics.recordApi("heartbeat", "redis_error");
            log.warn("Club popularity heartbeat recording failed: {}", exception.getMessage());
            return RecordingResult.REDIS_ERROR;
        }
    }

    private RecordingResult map(ClubPopularityRedisRepository.RecordResult result) {
        return switch (result) {
            case RECORDED -> RecordingResult.RECORDED;
            case RATE_LIMITED -> RecordingResult.RATE_LIMITED;
            case RECOVERING -> RecordingResult.RECOVERING;
        };
    }

    public enum RecordingResult {
        RECORDED,
        RATE_LIMITED,
        DISABLED,
        RECOVERING,
        REDIS_ERROR,
        INVALID_IDENTITY
    }
}
