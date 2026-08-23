package com.kakaotech.team18.backend_server.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 지원자 성별.
 * <p>
 * 통계 집계에만 사용되며, 선택 입력이다. 미입력(null)은 통계에서 {@code UNKNOWN} 버킷으로 노출된다.
 * <p>
 * <strong>반드시 {@code @Enumerated(EnumType.STRING)}으로 저장한다.</strong> 순서값으로 저장하면 상수를 추가하는 순간
 * 기존 데이터의 의미가 바뀐다.
 */
@Getter
@RequiredArgsConstructor
public enum Gender {
    MALE("남성"),
    FEMALE("여성");

    private final String label;
}
