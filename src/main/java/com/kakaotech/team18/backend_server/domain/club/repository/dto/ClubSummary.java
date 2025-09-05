package com.kakaotech.team18.backend_server.domain.club.repository.dto;

import com.kakaotech.team18.backend_server.domain.club.entity.Category;

import java.time.LocalDateTime;

public interface ClubSummary {
    Long getId();
    String getName();
    Category getCategory();
    String getShortIntroduction();
    LocalDateTime getRecruitStart();
    LocalDateTime getRecruitEnd();
}
