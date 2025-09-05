package com.kakaotech.team18.backend_server.domain.club.dto;

import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.repository.dto.ClubSummary;
import com.kakaotech.team18.backend_server.domain.club.service.model.RecruitStatus;
import com.kakaotech.team18.backend_server.domain.club.service.model.RecruitStatusLabel;

public record ClubResponse(
        Long id,
        String name,
        Category category,
        String shortIntroduction,
        String recruitStatus
) {
    public static ClubResponse from(Club club, RecruitStatus status) {
        return new ClubResponse(
                club.getId(),
                club.getName(),
                club.getCategory(),
                club.getShortIntroduction(),
                RecruitStatusLabel.toKorean(status)
        );
    }

    public static ClubResponse from(ClubSummary summary, RecruitStatus status) {
        return new ClubResponse(
                summary.id(),
                summary.name(),
                summary.category(),
                summary.shortIntroduction(),
                RecruitStatusLabel.toKorean(status)
        );
    }
}
