package com.kakaotech.team18.backend_server.domain.club.entity;

import com.kakaotech.team18.backend_server.domain.BaseEntity;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDetailRequestDto;
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
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class Club extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "club_id")
    private Long id;

    @Column(name = "club_name", nullable = false, unique = true)
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

    @Column(nullable = false)
    private Boolean isInterviewRequired;

    private LocalDateTime interviewStartDate;

    private LocalDateTime interviewEndDate;

    private LocalTime interviewStartTime;

    private LocalTime interviewEndTime;

    private String regularMeetingInfo;

    @Column(length = 2048)
    private String applicationUrl;

    @Column(nullable = false)
    private Boolean isExternal;

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
            Boolean isInterviewRequired,
            LocalDateTime interviewStartDate,
            LocalDateTime interviewEndDate,
            LocalTime interviewStartTime,
            LocalTime interviewEndTime,
            String regularMeetingInfo,
            String applicationUrl,
            Boolean isExternal) {
        this.name = name;
        this.category = category;
        this.location = location;
        this.shortIntroduction = shortIntroduction;
        this.introduction = (introduction != null) ? introduction : new ClubIntroduction();
        this.caution = caution;
        this.recruitStart = recruitStart;
        this.recruitEnd = recruitEnd;
        this.isInterviewRequired = (isInterviewRequired != null) ? isInterviewRequired : false;
        this.interviewStartDate = interviewStartDate;
        this.interviewEndDate = interviewEndDate;
        this.interviewStartTime = interviewStartTime;
        this.interviewEndTime = interviewEndTime;
        this.regularMeetingInfo = regularMeetingInfo;
        this.applicationUrl = applicationUrl;
        this.isExternal = (isExternal != null) ? isExternal : false;
    }

    public void updateDetail(ClubDetailRequestDto dto) {
        this.name = dto.clubName();
        this.category = dto.category();
        this.location = dto.location();
        this.shortIntroduction = dto.shortIntroduction();
        this.caution = dto.applicationNotice();
        this.regularMeetingInfo = dto.regularMeetingInfo();
        this.introduction.update(dto);
    }

    public void updateRecruitDate(LocalDateTime recruitStart, LocalDateTime recruitEnd) {
        this.recruitStart = recruitStart;
        this.recruitEnd = recruitEnd;
        log.info("Updated recruit date for clubId: {} to start: {} end: {}", this.id, recruitStart, recruitEnd);
    }

    public void updateInterviewDate(Boolean isInterviewRequired, LocalDateTime interviewStartDate,
            LocalDateTime interviewEndDate, LocalTime interviewStartTime, LocalTime interviewEndTime) {
        this.isInterviewRequired = isInterviewRequired;
        this.interviewStartDate = interviewStartDate;
        this.interviewEndDate = interviewEndDate;
        this.interviewStartTime = interviewStartTime;
        this.interviewEndTime = interviewEndTime;
        log.info(
                "Updated interview date for clubId: {} to interviewStartDate: {}, interviewEndDate: {}, interviewStartTime: {}, interviewEndTime: {}",
                this.id,
                interviewStartDate, interviewEndDate, interviewStartTime,
                interviewEndTime);
    }
}