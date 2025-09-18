package com.kakaotech.team18.backend_server.domain.club.dto;

import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.entity.ClubImage;
import com.kakaotech.team18.backend_server.domain.club.util.RecruitStatusCalculator;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Builder;

@Builder
public record ClubDetailResponseDto(
        String clubName,
        String location,
        Category category,
        String shortIntroduction,
        List<String> introductionImages,
        String introductionOverview,
        String introductionActivity,
        String introductionIdeal,
        String regularMeetingInfo,
        String recruitStatus,
        String presidentName,
        String presidentPhoneNumber,
        LocalDateTime recruitStart,
        LocalDateTime recruitEnd
        ) {

        public static ClubDetailResponseDto from(Club club, User user) {
                var intro = club.getIntroduction();
                var images = intro.getImages();

                return ClubDetailResponseDto.builder().
                        clubName(club.getName()).
                        location(club.getLocation()).
                        category(club.getCategory()).
                        shortIntroduction(club.getShortIntroduction()).
                        introductionImages(images.stream()
                                .map(ClubImage::getImageUrl)
                                .collect(Collectors.toList())).
                        introductionOverview(intro.getOverview()).
                        introductionActivity(intro.getActivities()).
                        introductionIdeal(intro.getIdeal()).
                        regularMeetingInfo(club.getRegularMeetingInfo()).
                        recruitStatus(RecruitStatusCalculator.calculate(club.getRecruitStart(), club.getRecruitEnd())).
                        presidentName(user.getName()).
                        presidentPhoneNumber(user.getPhoneNumber()).
                        recruitStart(club.getRecruitStart()).
                        recruitEnd(club.getRecruitEnd()).
                        build();
        }
}
