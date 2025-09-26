package com.kakaotech.team18.backend_server.domain.clubMember.entity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Role {
    APPLICANT("지원자"),
    SYSTEM_ADMIN("시스템 관리자"),
    CLUB_MEMBER("동아리 원"),
    CLUB_EXECUTIVE("동아리 운영진"),
    CLUB_ADMIN("동아리 회장");

    private final String text;
}
