package com.kakaotech.team18.backend_server.domain.club.dto;

import com.kakaotech.team18.backend_server.domain.club.entity.Category;

import java.time.LocalDateTime;

public interface ClubSummary {
    Long id();
    String name();
    Category category();
    String shortIntroduction();
    LocalDateTime recruitStart();
    LocalDateTime recruitEnd();
}
