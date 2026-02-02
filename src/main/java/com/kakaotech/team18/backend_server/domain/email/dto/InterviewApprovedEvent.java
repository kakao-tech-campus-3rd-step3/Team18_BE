package com.kakaotech.team18.backend_server.domain.email.dto;

import com.kakaotech.team18.backend_server.domain.application.entity.Stage;
import java.time.LocalDateTime;

public record InterviewApprovedEvent(
        ApplicationInfoDto info,
        Long applicationId,
        String email,
        String message,
        Stage stage,
        LocalDateTime interviewSchedule
) {}
