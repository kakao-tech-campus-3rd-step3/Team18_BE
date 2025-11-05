package com.kakaotech.team18.backend_server.domain.club.entity;


import static org.assertj.core.api.Assertions.assertThat;

import com.kakaotech.team18.backend_server.domain.club.dto.ClubDetailRequestDto;
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
                .applicationNotice("Updated caution")
                .build();

        club.updateDetail(dto);

        assertThat(club.getName().equals(dto.clubName()));
        assertThat(club.getCategory()).isEqualTo(dto.category());
        assertThat(club.getLocation()).isEqualTo(dto.location());
        assertThat(club.getShortIntroduction()).isEqualTo(dto.shortIntroduction());
        assertThat(club.getCaution()).isEqualTo(dto.applicationNotice());
        assertThat(club.getRegularMeetingInfo()).isEqualTo(dto.regularMeetingInfo());
        assertThat(club.getIntroduction().getOverview()).isEqualTo(dto.introductionOverview());
        assertThat(club.getIntroduction().getActivities()).isEqualTo(dto.introductionActivity());
        assertThat(club.getIntroduction().getIdeal()).isEqualTo(dto.introductionIdeal());
        assertThat(club.getIntroduction().getImages())
                .usingRecursiveComparison()
                .isEqualTo(List.of(
                        image1,
                        image2
                ));
    }
}