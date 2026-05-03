package com.kakaotech.team18.backend_server.domain.club.entity;


import static org.assertj.core.api.Assertions.assertThat;

import com.kakaotech.team18.backend_server.domain.club.dto.ClubDetailRequestDto;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClubTest {

    @DisplayName("updateDetail 메서드 테스트")
    @Test
    void updateDetailTest() {
        ClubImage image1 = ClubImage.builder().imageUrl("image1.jpg").build();
        ClubImage image2 = ClubImage.builder().imageUrl("image2.jpg").build();
        Club club = Club.builder()
                .name("Test Club")
                .category(Category.STUDY)
                .location("Test Location")
                .shortIntroduction("Short intro")
                .introduction(ClubIntroduction.builder()
                        .overview("Overview")
                        .activities("Activities")
                        .images(List.of(image1,
                                image2
                        ))
                        .ideal("Ideal")
                        .build())
                .caution("Caution")
                .regularMeetingInfo("Regular meeting info")
                .build();

        ClubDetailRequestDto dto = ClubDetailRequestDto.builder()
                .clubId(1L)
                .clubName("Updated Club")
                .category(Category.SPORTS)
                .location("Updated Location")
                .shortIntroduction("Updated short intro")
                .introductionOverview("Updated overview")
                .introductionActivity("Updated activities")
                .introductionIdeal("Updated ideal")
                .regularMeetingInfo("Updated regular meeting info")
                .isTelNoOpen(true)
                .applicationNotice("Updated caution")
                .everyTimeUrl("https://everytime.kr/test")
                .googleFormUrl("https://docs.google.com/forms/test")
                .instagramUrl("https://www.instagram.com/test")
                .build();

        club.updateDetail(dto);

        assertThat(club.getName()).isEqualTo(dto.clubName());
        assertThat(club.getCategory()).isEqualTo(dto.category());
        assertThat(club.getLocation()).isEqualTo(dto.location());
        assertThat(club.getShortIntroduction()).isEqualTo(dto.shortIntroduction());
        assertThat(club.getCaution()).isEqualTo(dto.applicationNotice());
        assertThat(club.getRegularMeetingInfo()).isEqualTo(dto.regularMeetingInfo());
        assertThat(club.isTelNoOpen()).isEqualTo(dto.isTelNoOpen());
        assertThat(club.getIntroduction().getOverview()).isEqualTo(dto.introductionOverview());
        assertThat(club.getIntroduction().getActivities()).isEqualTo(dto.introductionActivity());
        assertThat(club.getIntroduction().getIdeal()).isEqualTo(dto.introductionIdeal());
        assertThat(club.getEveryTimeUrl()).isEqualTo(dto.everyTimeUrl());
        assertThat(club.getGoogleFormUrl()).isEqualTo(dto.googleFormUrl());
        assertThat(club.getInstagramUrl()).isEqualTo(dto.instagramUrl());
        assertThat(club.getIntroduction().getImages())
                .usingRecursiveComparison()
                .isEqualTo(List.of(
                        image1,
                        image2
                ));
    }

    @DisplayName("updateInterview 메서드 테스트")
    @Test
    void updateInterviewTest() {
        Club club = Club.builder()
                .name("Test Club")
                .category(Category.STUDY)
                .location("Test Location")
                .shortIntroduction("Short intro")
                .introduction(ClubIntroduction.builder()
                        .overview("Overview")
                        .activities("Activities")
                        .ideal("Ideal")
                        .build())
                .caution("Caution")
                .regularMeetingInfo("Regular meeting info")
                .interviewStartDate(LocalDateTime.of(2024, 10, 1, 0, 0))
                .interviewEndDate(LocalDateTime.of(2024, 10, 3, 23, 59, 59))
                .interviewStartTime(LocalTime.of(9, 0))
                .interviewEndTime(LocalTime.of(18, 0))
                .build();

        LocalDateTime newStartDate = LocalDateTime.of(2024, 11, 1, 0, 0);
        LocalDateTime newEndDate = LocalDateTime.of(2024, 11, 5, 23, 59, 59);
        LocalTime newStartTime = LocalTime.of(10, 0);
        LocalTime newEndTime = LocalTime.of(17, 0);

        club.updateInterviewDate(true, newStartDate, newEndDate, newStartTime, newEndTime);

        assertThat(club.getIsInterviewRequired()).isTrue();
        assertThat(club.getInterviewStartDate()).isEqualTo(newStartDate);
        assertThat(club.getInterviewEndDate()).isEqualTo(newEndDate);
        assertThat(club.getInterviewStartTime()).isEqualTo(newStartTime);
        assertThat(club.getInterviewEndTime()).isEqualTo(newEndTime);

    }

}
