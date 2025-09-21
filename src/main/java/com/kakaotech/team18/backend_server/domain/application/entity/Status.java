package com.kakaotech.team18.backend_server.domain.application.entity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public enum Status {
    PENDING("미정"),
    APPROVED("합격"),
    REJECTED("불합격");

    private final String text;


    public String getText() {
        return text;
    }
}