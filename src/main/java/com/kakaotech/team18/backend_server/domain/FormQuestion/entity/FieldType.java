package com.kakaotech.team18.backend_server.domain.FormQuestion.entity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum FieldType {
    TEXT("주관식"),
    RADIO("라디오"),
    CHECKBOX("체크박스"),
    TIME_SLOT("타임슬롯");

    private final String text;
}
