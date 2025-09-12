package com.kakaotech.team18.backend_server.domain.club.entity;

import com.kakaotech.team18.backend_server.domain.BaseEntity;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
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

    private String introductionImage;

    private String introductionIntroduce;

    private String introductionActivity;

    private String introductionWannabe;

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
            String introductionImage,
            String introductionIntroduce,
            String introductionActivity,
            String introductionWannabe,
            LocalDateTime recruitStart,
            LocalDateTime recruitEnd,
            String regularMeetingInfo) {
        this.president = president;
        this.name = name;
        this.category = category;
        this.location = location;
        this.shortIntroduction = shortIntroduction;
        this.introductionImage = introductionImage;
        this.introductionIntroduce = introductionIntroduce;
        this.introductionActivity = introductionActivity;
        this.introductionWannabe = introductionWannabe;
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