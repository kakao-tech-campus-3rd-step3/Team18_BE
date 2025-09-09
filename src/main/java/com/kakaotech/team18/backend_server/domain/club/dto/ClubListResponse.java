package com.kakaotech.team18.backend_server.domain.club.dto;

import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.repository.dto.ClubSummary;

public record ClubListResponse(
        Long id,
        String name,
        Category category,
        String shortIntroduction,
        String recruitStatus
) {
    public static ClubListResponse from(Club club) {
        return new ClubListResponse(
                club.getId(),
                club.getName(),
                club.getCategory(),
                club.getShortIntroduction(),
                club.getRecruitStatus(club.getRecruitStart(), club.getRecruitEnd())
        );
    }

    public static ClubListResponse from(ClubSummary summary, String recruitStatus) {
        return new ClubListResponse(
                summary.id(),
                summary.name(),
                summary.category(),
                summary.shortIntroduction(),
                recruitStatus
        );
    }
}
