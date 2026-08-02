package com.kakaotech.team18.backend_server.domain.clubPopularity.service;

import com.kakaotech.team18.backend_server.domain.clubPopularity.config.ClubPopularityProperties;
import com.kakaotech.team18.backend_server.domain.clubPopularity.model.ClubPopularityTimePolicy;
import com.kakaotech.team18.backend_server.domain.clubPopularity.model.ClubPopularityViewerIdentity;
import com.kakaotech.team18.backend_server.domain.clubPopularity.redis.ClubPopularityRedisRepository;
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

    public RecordingResult recordView(long clubId, Authentication authentication, String anonymousId) {
        if (!properties.isEnabled()) {
            return RecordingResult.DISABLED;
        }
        Optional<ClubPopularityViewerIdentity> identity = viewerResolver.resolve(authentication, anonymousId);
        if (identity.isEmpty()) {
            return RecordingResult.INVALID_IDENTITY;
        }
        try {
            long nowMillis = timePolicy.currentInstant().toEpochMilli();
            return map(redisRepository.recordView(clubId, identity.get(), nowMillis,
                    properties.getViewMinIntervalSeconds(), properties.getActiveTtlSeconds(),
                    (int) properties.getRetentionHours() * 60 * 60));
        } catch (DataAccessException exception) {
            log.warn("Club popularity view recording failed: {}", exception.getMessage());
            return RecordingResult.REDIS_ERROR;
        }
    }

    public RecordingResult recordHeartbeat(long clubId, Authentication authentication, String anonymousId) {
        if (!properties.isEnabled()) {
            return RecordingResult.DISABLED;
        }
        Optional<ClubPopularityViewerIdentity> identity = viewerResolver.resolve(authentication, anonymousId);
        if (identity.isEmpty()) {
            return RecordingResult.INVALID_IDENTITY;
        }
        try {
            long nowMillis = timePolicy.currentInstant().toEpochMilli();
            return map(redisRepository.recordHeartbeat(clubId, identity.get(), nowMillis,
                    properties.getHeartbeatMinIntervalSeconds(), properties.getActiveTtlSeconds()));
        } catch (DataAccessException exception) {
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
