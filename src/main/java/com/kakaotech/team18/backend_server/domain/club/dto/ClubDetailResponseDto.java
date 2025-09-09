package com.kakaotech.team18.backend_server.domain.club.dto;

import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.user.entity.Users;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record ClubDetailResponseDto(
        String clubName,
        String location,
        Category category,
        String shortIntroduction,
        String introductionImage,
        String introductionIntroduce,
        String introductionActivity,
        String introductionWannabe,
        String regularMeetingInfo,
        String recruitStatus,
        String presidentName,
        String presidentPhoneNumber,
        LocalDateTime recruitStart,
        LocalDateTime recruitEnd
        ) {

        public static ClubDetailResponseDto from(Club club, Users users) {
                return ClubDetailResponseDto.builder().
                        clubName(club.getName()).
                        location(club.getLocation()).
                        category(club.getCategory()).
                        shortIntroduction(club.getShortIntroduction()).
                        introductionImage(club.getIntroductionImage()).
                        introductionIntroduce(club.getIntroductionIntroduce()).
                        introductionActivity(club.getIntroductionActivity()).
                        introductionWannabe(club.getIntroductionWannabe()).
                        regularMeetingInfo(club.getRegularMeetingInfo()).
                        recruitStatus(club.getRecruitStatus(club.getRecruitStart(), club.getRecruitEnd())).
                        presidentName(users.getName()).
                        presidentPhoneNumber(users.getPhoneNumber()).
                        recruitStart(club.getRecruitStart()).
                        recruitEnd(club.getRecruitEnd()).
                        build();
        }
}
