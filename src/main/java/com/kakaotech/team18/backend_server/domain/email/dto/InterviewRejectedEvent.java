package com.kakaotech.team18.backend_server.domain.email.dto;

import com.kakaotech.team18.backend_server.domain.application.entity.Stage;
import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.user.entity.User;

public record InterviewRejectedEvent(
        Club club,
        User user,
        String email,
        Stage stage,
        Status status
){}
