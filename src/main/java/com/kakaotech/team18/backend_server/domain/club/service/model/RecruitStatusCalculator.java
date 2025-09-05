package com.kakaotech.team18.backend_server.domain.club.service.model;

import java.time.Clock;
import java.time.LocalDateTime;

public final class RecruitStatusCalculator {
    private RecruitStatusCalculator() {}

    public static RecruitStatus of(LocalDateTime start, LocalDateTime end, Clock clock) {
        if (start == null || end == null) return RecruitStatus.UNDECIDED;

        LocalDateTime now = LocalDateTime.now(clock);
        if (now.isBefore(start)) return RecruitStatus.PREPARE;
        if (!now.isBefore(end))  return RecruitStatus.CLOSED;
        return RecruitStatus.OPEN;
    }
}