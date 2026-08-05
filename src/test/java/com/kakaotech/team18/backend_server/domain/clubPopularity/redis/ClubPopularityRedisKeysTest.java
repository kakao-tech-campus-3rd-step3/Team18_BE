package com.kakaotech.team18.backend_server.domain.clubPopularity.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kakaotech.team18.backend_server.domain.clubPopularity.model.ClubPopularityViewerIdentity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClubPopularityRedisKeysTest {

    @Test
    @DisplayName("인기 기능의 Redis 키 형식을 중앙 규칙으로 생성한다")
    void createsRedisKeys() {
        assertThat(ClubPopularityRedisKeys.recentViewers(7)).isEqualTo("club:popularity:recent:7");
        assertThat(ClubPopularityRedisKeys.activeViewers(7)).isEqualTo("club:popularity:active:7");
        assertThat(ClubPopularityRedisKeys.PENDING).isEqualTo("club:popularity:pending");
        assertThat(ClubPopularityRedisKeys.CANDIDATES).isEqualTo("club:popularity:candidates");
        assertThat(ClubPopularityRedisKeys.KNOWN_CLUBS).isEqualTo("club:popularity:known-clubs");
        assertThat(ClubPopularityRedisKeys.rateLimit("views", 7, "U:15"))
                .isEqualTo("club:popularity:rate-limit:views:7:U:15");
    }

    @Test
    @DisplayName("DB 저장 대기 구성원은 버전·동아리·식별자 유형을 함께 보존한다")
    void serializesAndParsesPendingMember() {
        String user = ClubPopularityPendingKey.serialize(7, ClubPopularityViewerIdentity.user(15L));
        String anonymous = ClubPopularityPendingKey.serialize(7, ClubPopularityViewerIdentity.anonymous("익명 사용자"));

        assertThat(user).isEqualTo("v1|7|U|15");
        assertThat(ClubPopularityPendingKey.parse(user)).isEqualTo(
                new ClubPopularityPendingKey.Parsed(7, "U", "15"));
        assertThat(ClubPopularityPendingKey.parse(anonymous).type()).isEqualTo("A");
        assertThat(ClubPopularityPendingKey.parse(anonymous).value()).doesNotContain("|");
    }

    @Test
    @DisplayName("알 수 없는 버전이나 형식의 DB 대기 구성원은 거부한다")
    void rejectsInvalidPendingMember() {
        assertThatThrownBy(() -> ClubPopularityPendingKey.parse("v2|7|U|15"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ClubPopularityPendingKey.parse("v1|not-a-club|U|15"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
