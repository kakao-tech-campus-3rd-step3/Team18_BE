package com.kakaotech.team18.backend_server.domain.clubPopularity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.kakaotech.team18.backend_server.domain.clubPopularity.model.ClubPopularityViewerIdentity;
import com.kakaotech.team18.backend_server.global.security.PrincipalDetails;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

class ClubPopularityViewerResolverTest {

    private ClubPopularityViewerResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new ClubPopularityViewerResolver();
    }

    @Test
    @DisplayName("로그인 사용자는 익명 식별자보다 우선한다")
    void loggedInUserTakesPriority() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new PrincipalDetails(15L, Map.of()));

        ClubPopularityViewerIdentity identity = resolver.resolve(authentication, "anonymous-before-login")
                .orElseThrow();

        assertThat(identity.type()).isEqualTo(ClubPopularityViewerIdentity.Type.USER);
        assertThat(identity.redisMember()).isEqualTo("U:15");
    }

    @Test
    @DisplayName("비로그인 사용자의 원문 식별자를 Base64URL Redis 구성원으로 인코딩한다")
    void resolvesAnonymousIdentityWithoutNormalization() {
        String anonymousId = " 익명-사용자/값 ";

        ClubPopularityViewerIdentity identity = resolver.resolve(null, anonymousId).orElseThrow();

        assertThat(identity.type()).isEqualTo(ClubPopularityViewerIdentity.Type.ANONYMOUS);
        assertThat(identity.value()).isEqualTo(anonymousId);
        assertThat(identity.redisMember()).startsWith("A:").doesNotContain("=");
    }

    @Test
    @DisplayName("익명 식별자는 UTF-8 기준 512바이트까지 허용한다")
    void acceptsExactly512Utf8Bytes() {
        String anonymousId = "가".repeat(170) + "ab";
        assertThat(anonymousId.getBytes(StandardCharsets.UTF_8)).hasSize(512);

        assertThat(resolver.resolve(null, anonymousId)).isPresent();
    }

    @Test
    @DisplayName("익명 식별자가 UTF-8 기준 512바이트를 넘으면 거부한다")
    void rejectsMoreThan512Utf8Bytes() {
        String anonymousId = "가".repeat(171);
        assertThat(anonymousId.getBytes(StandardCharsets.UTF_8)).hasSize(513);

        assertThat(resolver.resolve(null, anonymousId)).isEmpty();
    }

    @Test
    @DisplayName("빈 익명 식별자는 거부한다")
    void rejectsBlankAnonymousIdentity() {
        assertThat(resolver.resolve(null, "  ")).isEmpty();
    }
}
