package com.kakaotech.team18.backend_server.domain.club.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RecruitStatusCalculatorTest {

    @Test
    @DisplayName("고정된 시간을 기준으로 모집 상태가 올바르게 계산되어야 한다")
    void calculateRecruitStatus_withFixedTime() {
        // given
        final LocalDateTime startDate = LocalDateTime.of(2025, 5, 15, 0, 0);
        final LocalDateTime endDate = LocalDateTime.of(2025, 5, 20, 23, 59);

        // 1. 모집 시작 전
        final LocalDateTime beforeRecruiting = LocalDateTime.of(2025, 5, 10, 0, 0);

        // 2. 모집 기간 중
        final LocalDateTime duringRecruiting = LocalDateTime.of(2025, 5, 16, 12, 0);

        // 3. 모집 종료 후
        final LocalDateTime afterRecruiting = LocalDateTime.of(2025, 5, 21, 0, 0);

        // when
        // package-private 메소드를 직접 호출하여 테스트
        String statusBefore = RecruitStatusCalculator.calculate(startDate, endDate, beforeRecruiting);
        String statusDuring = RecruitStatusCalculator.calculate(startDate, endDate, duringRecruiting);
        String statusAfter = RecruitStatusCalculator.calculate(startDate, endDate, afterRecruiting);

        // then
        assertThat(statusBefore).isEqualTo("모집 준비중");
        assertThat(statusDuring).isEqualTo("모집중");
        assertThat(statusAfter).isEqualTo("모집 종료");
    }

    @Test
    @DisplayName("모집 기간이 null일 경우 '모집 일정 미정'을 반환해야 한다")
    void calculateRecruitStatus_withNullDates() {
        // given
        final LocalDateTime now = LocalDateTime.now();

        // when
        String statusWithNullStart = RecruitStatusCalculator.calculate(null, now, now);
        String statusWithNullEnd = RecruitStatusCalculator.calculate(now, null, now);
        String statusWithBothNull = RecruitStatusCalculator.calculate(null, null, now);

        // then
        assertThat(statusWithNullStart).isEqualTo("모집 일정 미정");
        assertThat(statusWithNullEnd).isEqualTo("모집 일정 미정");
        assertThat(statusWithBothNull).isEqualTo("모집 일정 미정");
    }
}