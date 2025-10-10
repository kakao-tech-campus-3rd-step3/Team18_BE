package com.kakaotech.team18.backend_server.domain.club.entity;

import com.kakaotech.team18.backend_server.domain.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Club extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "club_id")
    private Long id;

    @Column(name = "club_name",  nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    private String location;

    private String shortIntroduction;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "club_introduction_id")
    private ClubIntroduction introduction;

    private String caution;

    private LocalDateTime recruitStart;

    private LocalDateTime recruitEnd;

    private String regularMeetingInfo;

    @Builder
    private Club(
            String name,
            Category category,
            String location,
            String shortIntroduction,
            ClubIntroduction introduction,
            String caution,
            LocalDateTime recruitStart,
            LocalDateTime recruitEnd,
            String regularMeetingInfo) {
        this.name = name;
        this.category = category;
        this.location = location;
        this.shortIntroduction = shortIntroduction;
        this.introduction = introduction;
        this.caution = caution;
        this.recruitStart = recruitStart;
        this.recruitEnd = recruitEnd;
        this.regularMeetingInfo = regularMeetingInfo;
    }
}