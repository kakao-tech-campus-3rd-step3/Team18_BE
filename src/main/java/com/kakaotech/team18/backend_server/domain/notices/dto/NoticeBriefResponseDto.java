package com.kakaotech.team18.backend_server.domain.notices.dto;

import java.time.LocalDateTime;

public record NoticeBriefResponseDto(
        Long id,
        String title,
        LocalDateTime createdAt,
        String author
) {
}
