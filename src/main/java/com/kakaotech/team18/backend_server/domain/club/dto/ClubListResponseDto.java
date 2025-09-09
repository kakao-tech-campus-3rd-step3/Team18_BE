package com.kakaotech.team18.backend_server.domain.club.dto;

import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;

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
                club.getRecruitStatus(club.getRecruitStart(), club.getRecruitEnd())
        );
    }

    public static ClubListResponseDto from(ClubSummary summary, String recruitStatus) {
        return new ClubListResponseDto(
                summary.id(),
                summary.name(),
                summary.category(),
                summary.shortIntroduction(),
                recruitStatus
        );
    }
}
