package com.kakaotech.team18.backend_server.domain.club.service.model;

public final class RecruitStatusLabel {
    private RecruitStatusLabel() {}

    public static String toKorean(RecruitStatus status) {
        return switch (status) {
            case UNDECIDED -> "모집 일정 미정";
            case PREPARE   -> "모집 준비중";
            case OPEN      -> "모집중";
            case CLOSED    -> "모집 종료";
        };
    }
}
