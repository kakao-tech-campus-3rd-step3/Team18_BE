package com.kakaotech.team18.backend_server.domain.clubPopularity.model;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

public record ClubPopularityViewerIdentity(Type type, String value) {

    public enum Type {
        USER,
        ANONYMOUS
    }

    public ClubPopularityViewerIdentity {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(value, "value must not be null");
    }

    public static ClubPopularityViewerIdentity user(Long userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        return new ClubPopularityViewerIdentity(Type.USER, userId.toString());
    }

    public static ClubPopularityViewerIdentity anonymous(String anonymousId) {
        return new ClubPopularityViewerIdentity(Type.ANONYMOUS, anonymousId);
    }

    public String redisMember() {
        if (type == Type.USER) {
            // 로그인 사용자와 익명 사용자가 같은 키를 쓰지 않도록 접두사를 분리한다.
            return "U:" + value;
        }
        // 원문 익명 ID는 보존하고 Redis 구성원 값만 URL-safe하게 인코딩한다.
        String encoded = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
        return "A:" + encoded;
    }
}
