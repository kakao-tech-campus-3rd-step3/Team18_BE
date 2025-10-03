package com.kakaotech.team18.backend_server.domain.email.dto;

import com.kakaotech.team18.backend_server.domain.application.entity.Stage;

public record FinalRejectedEvent(
        Long applicationId,
        String email,
        Stage stage
) {}
