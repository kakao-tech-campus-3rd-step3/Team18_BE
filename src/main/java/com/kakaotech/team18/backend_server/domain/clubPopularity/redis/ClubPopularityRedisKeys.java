package com.kakaotech.team18.backend_server.domain.clubPopularity.redis;

public final class ClubPopularityRedisKeys {

    public static final String PENDING = "club:popularity:pending";
    public static final String CANDIDATES = "club:popularity:candidates";
    public static final String KNOWN_CLUBS = "club:popularity:known-clubs";
    public static final String KNOWN_CLUBS_READY = "club:popularity:known-clubs:ready";
    public static final String RECOVERY_STATUS = "club:popularity:recovery:status";
    public static final String FLUSH_LOCK = "club:popularity:lock:flush";
    public static final String FAILED_RETRY = "club:popularity:failed:retry";
    public static final String RECOVERY_LOCK = "club:popularity:lock:recovery";

    private ClubPopularityRedisKeys() {
    }

    public static String recentViewers(long clubId) {
        return "club:popularity:recent:" + clubId;
    }

    public static String activeViewers(long clubId) {
        return "club:popularity:active:" + clubId;
    }

    public static String rateLimit(String apiType, long clubId, String identity) {
        return "club:popularity:rate-limit:" + apiType + ":" + clubId + ":" + identity;
    }

    public static String failedData(String failureId) {
        return "club:popularity:failed:data:" + failureId;
    }
}
