package com.kakaotech.team18.backend_server.domain.application.entity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Status {
    PENDING("미정"),
    APPROVED("합격"),
    REJECTED("불합격");

    private final String text;
}