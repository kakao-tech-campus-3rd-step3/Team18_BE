package com.kakaotech.team18.backend_server.domain.clubPopularity.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import java.time.Instant;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

class ClubViewTest {

    @Test
    void storesExactlyOneViewerIdentity() {
        Club club = org.mockito.Mockito.mock(Club.class);
        ClubView user = ClubView.user(club, 15L, Instant.ofEpochMilli(1));
        ClubView anonymous = ClubView.anonymous(club, new byte[]{1, 2}, Instant.ofEpochMilli(2));
        assertThat(user.getUserId()).isEqualTo(15L);
        assertThat(user.getAnonymousIdentity()).isNull();
        assertThat(anonymous.getUserId()).isNull();
        assertThat(anonymous.getAnonymousIdentity()).containsExactly(1, 2);
        assertThat(user.redisMember()).isEqualTo("U:15");
        assertThat(anonymous.redisMember()).isEqualTo("A:AQI");
    }

    @Test
    void rejectsMissingOrMultipleViewerIdentities() {
        Club club = org.mockito.Mockito.mock(Club.class);

        assertThatThrownBy(() -> ClubView.anonymous(club, null, Instant.EPOCH))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
