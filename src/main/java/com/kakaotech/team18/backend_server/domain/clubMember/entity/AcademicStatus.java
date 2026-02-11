package com.kakaotech.team18.backend_server.domain.clubMember.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AcademicStatus {
    ENROLLED("재학"),
    LEAVE_OF_ABSENCE("휴학"),
    GRADUATED("졸업"),
    COMPLETED("수료"),
    EXPELLED("제적");

    private final String description;
}
