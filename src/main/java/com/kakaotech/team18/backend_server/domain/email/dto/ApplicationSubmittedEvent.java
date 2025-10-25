package com.kakaotech.team18.backend_server.domain.email.dto;

import java.util.List;

public record ApplicationSubmittedEvent(
        ApplicationInfoDto info,
        Long applicationId,
        List<AnswerEmailLine> emailLines
) {}
