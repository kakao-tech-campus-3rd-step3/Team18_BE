package com.kakaotech.team18.backend_server.domain.application.util;

import java.util.regex.Pattern;

/**
 * 학과 문자열을 통계 집계에 쓸 수 있는 형태로 정규화합니다.
 * <p>
 * 학과는 자유 입력이므로 표기 파편화(`컴퓨터공학과`/`컴퓨터공학부`/`컴공`)를 완전히 막을 수 없습니다. 여기서는
 * <strong>공백 제거만</strong> 수행해 `컴퓨터 공학과`와 `컴퓨터공학과`가 같은 버킷으로 묶이게 합니다. 그 이상의
 * 정규화(유사어 통합)는 하지 않으며, 남는 파편화는 통계 응답의 주의 문구로 알립니다.
 */
public final class DepartmentNormalizer {

    /** 학과 미입력 시 사용하는 대체 값. */
    public static final String UNKNOWN_DEPARTMENT = "미입력";

    /** 앞뒤뿐 아니라 문자열 중간의 공백까지 모두 제거하기 위한 패턴. */
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private DepartmentNormalizer() {
    }

    /**
     * 학과 문자열의 모든 공백을 제거합니다.
     *
     * @param raw 사용자가 입력한 학과 문자열
     * @return 공백이 제거된 학과명, 입력이 비어 있으면 {@value #UNKNOWN_DEPARTMENT}
     */
    public static String normalize(String raw) {
        if (raw == null) {
            return UNKNOWN_DEPARTMENT;
        }
        String normalized = WHITESPACE.matcher(raw).replaceAll("");
        return normalized.isEmpty() ? UNKNOWN_DEPARTMENT : normalized;
    }
}
