package com.kakaotech.team18.backend_server.global.util;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DateUtilTest {

    @DisplayName("인터뷰 선호 시간 파싱 성공")
    @Test
    void parseDateAndTimeSlots_success() {
        //given
        String dateInfo = "2025-10-16 10:30-11:00,2025-10-16 11:00-11:30";

        //when
        Map<LocalDate, List<LocalTime>> result = DateUtil.parseDateAndTimeSlots(dateInfo);

        //then
        LocalDate dateResult = LocalDate.of(2025, 10, 16);
        Assertions.assertThat(result.containsKey(dateResult)).isTrue();
        Assertions.assertThat(result.get(dateResult)).isEqualTo(
                List.of(LocalTime.of(10, 30), LocalTime.of(11, 0)));
    }

}