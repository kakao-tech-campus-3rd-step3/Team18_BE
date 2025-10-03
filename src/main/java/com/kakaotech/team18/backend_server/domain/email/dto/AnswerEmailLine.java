package com.kakaotech.team18.backend_server.domain.email.dto;

public record AnswerEmailLine(
        Long questionId,
        Long displayOrder,
        String question,
        String answer
) {}
