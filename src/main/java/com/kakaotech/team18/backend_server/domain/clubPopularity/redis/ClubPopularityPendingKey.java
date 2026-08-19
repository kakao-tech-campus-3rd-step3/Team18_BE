package com.kakaotech.team18.backend_server.domain.clubPopularity.redis;

import com.kakaotech.team18.backend_server.domain.clubPopularity.model.ClubPopularityViewerIdentity;

public final class ClubPopularityPendingKey {

    private static final String VERSION = "v1";

    private ClubPopularityPendingKey() {
    }

    public static String serialize(long clubId, ClubPopularityViewerIdentity identity) {
        String type = identity.type() == ClubPopularityViewerIdentity.Type.USER ? "U" : "A";
        String value = identity.type() == ClubPopularityViewerIdentity.Type.USER
                ? identity.value()
                : identity.redisMember().substring(2);
        return VERSION + "|" + clubId + "|" + type + "|" + value;
    }

    public static Parsed parse(String value) {
        if (value == null) {
            throw new IllegalArgumentException("pending key must not be null");
        }
        String[] parts = value.split("\\|", -1);
        if (parts.length != 4 || !VERSION.equals(parts[0]) || parts[1].isBlank()
                || !("U".equals(parts[2]) || "A".equals(parts[2])) || parts[3].isBlank()) {
            throw new IllegalArgumentException("invalid pending key format");
        }
        try {
            return new Parsed(Long.parseLong(parts[1]), parts[2], parts[3]);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("invalid club id in pending key", exception);
        }
    }

    public record Parsed(long clubId, String type, String value) {
    }
}
