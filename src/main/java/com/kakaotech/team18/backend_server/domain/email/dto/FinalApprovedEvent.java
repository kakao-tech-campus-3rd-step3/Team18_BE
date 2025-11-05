package com.kakaotech.team18.backend_server.domain.email.dto;

import com.kakaotech.team18.backend_server.domain.application.entity.Stage;

public record FinalApprovedEvent(
        ApplicationInfoDto info,
        Long applicationId,
        String email,
        String message,
        Stage stage
) {}
