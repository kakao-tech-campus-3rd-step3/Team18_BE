package com.kakaotech.team18.backend_server.domain.clubPopularity.entity;

import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/** DB에 남기는 최신 조회 시각이다. 실시간 집계용 Redis와 분리한다. */
@Entity
@Table(name = "club_view", uniqueConstraints = {
        @UniqueConstraint(name = "uk_club_view_user", columnNames = {"club_id", "user_id"}),
        @UniqueConstraint(name = "uk_club_view_anonymous", columnNames = {"club_id", "anonymous_identity"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClubView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "club_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Club club;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "anonymous_identity", columnDefinition = "VARBINARY(512)")
    private byte[] anonymousIdentity;

    @Column(name = "last_viewed_at", nullable = false)
    private Instant lastViewedAt;

    private ClubView(Club club, Long userId, byte[] anonymousIdentity, Instant lastViewedAt) {
        if ((userId == null) == (anonymousIdentity == null)) {
            throw new IllegalArgumentException("exactly one viewer identity is required");
        }
        this.club = club;
        this.userId = userId;
        this.anonymousIdentity = anonymousIdentity;
        this.lastViewedAt = lastViewedAt;
    }

    public static ClubView user(Club club, long userId, Instant lastViewedAt) {
        return new ClubView(club, userId, null, lastViewedAt);
    }

    public static ClubView anonymous(Club club, byte[] anonymousIdentity, Instant lastViewedAt) {
        return new ClubView(club, null, anonymousIdentity.clone(), lastViewedAt);
    }

    public String redisMember() {
        if (userId != null) {
            return "U:" + userId;
        }
        return "A:" + java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(anonymousIdentity);
    }
}
