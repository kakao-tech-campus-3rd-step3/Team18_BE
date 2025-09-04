package com.kakaotech.team18.backend_server.domain.club.dto;

import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import java.time.LocalDateTime;

public record ClubResponse(
        Long id,
        String name,
        Category category,
        String shortIntroduction,
        String recruitStatus
) {
    public static ClubResponse from(Club club) {
        return new ClubResponse(
                club.getId(),
                club.getName(),
                club.getCategory(),
                club.getShortIntroduction(),
                getRecruitStatus(club.getRecruitStart(), club.getRecruitEnd())
        );
    }

    private static String getRecruitStatus(LocalDateTime start, LocalDateTime end) {
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
