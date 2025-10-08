package com.kakaotech.team18.backend_server.domain.club.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RecruitStatus {
    NOT_SCHEDULED("모집 일정 미정"),
    PREPARING("모집 준비중"),
    RECRUITING("모집중"),
    CLOSED("모집 종료");

    private final String displayName;
}