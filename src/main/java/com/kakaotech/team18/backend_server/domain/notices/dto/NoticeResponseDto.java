package com.kakaotech.team18.backend_server.domain.notices.dto;

import java.time.LocalDateTime;

public record NoticeResponseDto(
        Long id,
        String title,
        String content,
        LocalDateTime createdAt,
        String author,
        String email
) {
}
