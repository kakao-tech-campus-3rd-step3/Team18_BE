package com.kakaotech.team18.backend_server.domain.club.dto;

import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.repository.dto.ClubSummary;

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
                club.getRecruitStatus(club.getRecruitStart(), club.getRecruitEnd())
        );
    }

    public static ClubResponse from(ClubSummary summary, String recruitStatus) {
        return new ClubResponse(
                summary.id(),
                summary.name(),
                summary.category(),
                summary.shortIntroduction(),
                recruitStatus
        );
    }
}
