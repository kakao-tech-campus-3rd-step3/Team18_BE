package com.kakaotech.team18.backend_server.domain.club.entity;

import com.kakaotech.team18.backend_server.domain.BaseEntity;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import jakarta.persistence.*;

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
    private long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "president_id")
    private User president;

    @Column(name = "club_name")
    private String name;

    @Enumerated(EnumType.STRING)
    private Category category;

    private String location;

    private String shortIntroduction;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "club_introduction_id")
    private ClubIntroduction introduction;

    private LocalDateTime recruitStart;

    private LocalDateTime recruitEnd;

    private String regularMeetingInfo;

    @Builder
    private Club(
            User president,
            String name,
            Category category,
            String location,
            String shortIntroduction,
            ClubIntroduction introduction,
            LocalDateTime recruitStart,
            LocalDateTime recruitEnd,
            String regularMeetingInfo) {
        this.president = president;
        this.name = name;
        this.category = category;
        this.location = location;
        this.shortIntroduction = shortIntroduction;
        this.introduction = introduction;
        this.recruitStart = recruitStart;
        this.recruitEnd = recruitEnd;
        this.regularMeetingInfo = regularMeetingInfo;
    }

    public String getRecruitStatus(LocalDateTime start, LocalDateTime end) {
        LocalDateTime today = LocalDateTime.now();

        if (start == null || end == null) {
            return "모집 일정 미정";
        }

        if (today.isBefore(start)) {
            return "모집 준비중";
        } else if (!today.isBefore(end)) {
            return "모집 종료";
        } else {
            return "모집중";
        }
    }

}