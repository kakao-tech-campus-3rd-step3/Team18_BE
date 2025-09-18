package com.kakaotech.team18.backend_server.domain.application.entity;

import com.kakaotech.team18.backend_server.global.exception.exceptions.StatusNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public enum Status {
    PENDING("미정"),
    APPROVED("합격"),
    REJECTED("불합격");

    private final String text;

    public static Status fromText(String text) {
        for (Status s : values()) {
            if (s.text.equals(text)) {
                return s;
            }
        }
        log.warn("Unknown status: {}", text);
        throw new StatusNotFoundException("잘못된 상태값: " + text);
    }
}