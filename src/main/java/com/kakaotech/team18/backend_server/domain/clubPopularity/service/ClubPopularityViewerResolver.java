package com.kakaotech.team18.backend_server.domain.clubPopularity.service;

import com.kakaotech.team18.backend_server.domain.clubPopularity.model.ClubPopularityViewerIdentity;
import com.kakaotech.team18.backend_server.global.security.PrincipalDetails;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class ClubPopularityViewerResolver {

    public static final int MAX_ANONYMOUS_ID_BYTES = 512;

    public Optional<ClubPopularityViewerIdentity> resolve(Authentication authentication, String anonymousId) {
        // JWT가 있으면 여러 기기에서도 동일한 로그인 사용자로 집계한다.
        if (authentication != null && authentication.getPrincipal() instanceof PrincipalDetails principalDetails) {
            return Optional.of(ClubPopularityViewerIdentity.user(principalDetails.getUserId()));
        }
        if (anonymousId == null || anonymousId.isBlank()) {
            return Optional.empty();
        }
        // 문자열 길이가 아닌 실제 저장 기준인 UTF-8 바이트 수를 검증한다.
        if (anonymousId.getBytes(StandardCharsets.UTF_8).length > MAX_ANONYMOUS_ID_BYTES) {
            return Optional.empty();
        }
        return Optional.of(ClubPopularityViewerIdentity.anonymous(anonymousId));
    }
}
