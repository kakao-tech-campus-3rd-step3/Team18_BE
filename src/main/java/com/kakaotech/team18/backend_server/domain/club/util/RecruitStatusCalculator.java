package com.kakaotech.team18.backend_server.domain.club.util;

import java.time.LocalDateTime;

/**
 * 동아리 모집 상태(RecruitStatus)를 계산하는 로직을 중앙에서 관리하는 유틸리티 클래스
 */
public final class RecruitStatusCalculator {

    private RecruitStatusCalculator() {
    }

    /**
     * 실제 애플리케이션에서 사용할 Public API 입니다.
     * 내부적으로 현재 시간을 사용하여 모집 상태를 계산합니다.
     *
     * @param start 모집 시작일
     * @param end   모집 종료일
     * @return 계산된 모집 상태 문자열
     */
    public static String calculate(LocalDateTime start, LocalDateTime end) {
        return calculate(start, end, LocalDateTime.now());
    }

    /**
     * 테스트 코드에서만 사용하기 위한 package-private 메소드입니다.
     * 'now'를 외부에서 주입받아 예측 가능한 테스트를 가능하게 합니다.
     *
     * @param start 모집 시작일
     * @param end   모집 종료일
     * @param now   기준 시간
     * @return 계산된 모집 상태 문자열
     */
    static String calculate(LocalDateTime start, LocalDateTime end, LocalDateTime now) {
        if (start == null || end == null) {
            return "모집 일정 미정";
        }

        if (now.isBefore(start)) {
            return "모집 준비중";
        } else if (!now.isBefore(end)) {
            return "모집 종료";
        } else {
            return "모집중";
        }
    }
}