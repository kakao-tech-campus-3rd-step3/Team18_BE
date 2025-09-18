package com.kakaotech.team18.backend_server.domain.club.dto;

import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.util.RecruitStatusCalculator;

public record ClubListResponseDto(
        Long id,
        String name,
        Category category,
        String shortIntroduction,
        String recruitStatus
) {
    public static ClubListResponseDto from(Club club) {
        return new ClubListResponseDto(
                club.getId(),
                club.getName(),
                club.getCategory(),
                club.getShortIntroduction(),
                RecruitStatusCalculator.calculate(club.getRecruitStart(), club.getRecruitEnd())
        );
    }

    public static ClubListResponseDto from(ClubSummary summary, String recruitStatus) {
        return new ClubListResponseDto(
                summary.getId(),
                summary.getName(),
                summary.getCategory(),
                summary.getShortIntroduction(),
                recruitStatus
        );
    }
}
