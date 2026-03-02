package com.kakaotech.team18.backend_server.domain.activity.entity;

import com.kakaotech.team18.backend_server.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "activity_daily",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_activity_daily_user", columnNames = {"activity_date", "user_id"}),
                @UniqueConstraint(name = "uk_activity_daily_anonymous", columnNames = {"activity_date", "anonymous_id"})
        }
)
public class ActivityDaily extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_daily_id")
    private Long id;

    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate;

    @Column(name = "anonymous_id")
    private String anonymousId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "first_seen_at", nullable = false)
    private LocalDateTime firstSeenAt;

    @Column(name = "last_seen_at", nullable = false)
    private LocalDateTime lastSeenAt;

    @Column(name = "hit_count", nullable = false)
    private Integer hitCount;

    @Builder
    private ActivityDaily(LocalDate activityDate, String anonymousId, Long userId,
            LocalDateTime firstSeenAt, LocalDateTime lastSeenAt, Integer hitCount) {
        this.activityDate = activityDate;
        this.anonymousId = anonymousId;
        this.userId = userId;
        this.firstSeenAt = firstSeenAt;
        this.lastSeenAt = lastSeenAt;
        this.hitCount = hitCount;
    }

    public static ActivityDaily forUser(LocalDate activityDate, Long userId, LocalDateTime now) {
        return ActivityDaily.builder()
                .activityDate(activityDate)
                .userId(userId)
                .anonymousId(null)
                .firstSeenAt(now)
                .lastSeenAt(now)
                .hitCount(1)
                .build();
    }

    public static ActivityDaily forAnonymous(LocalDate activityDate, String anonymousId, LocalDateTime now) {
        return ActivityDaily.builder()
                .activityDate(activityDate)
                .userId(null)
                .anonymousId(anonymousId)
                .firstSeenAt(now)
                .lastSeenAt(now)
                .hitCount(1)
                .build();
    }

    public void touch(LocalDateTime now) {
        this.lastSeenAt = now;
        this.hitCount = this.hitCount + 1;
    }
}
