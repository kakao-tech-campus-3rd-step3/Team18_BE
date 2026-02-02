package com.kakaotech.team18.backend_server.domain.application.entity;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ApplicationTest {

    @DisplayName("인터뷰 선호 시간 정보 업데이트 성공")
    @Test
    void updatePreferInterviewInfo_success() {
        //given
        LocalDate date = LocalDate.of(2025, 10, 16);
        List<LocalTime> times = List.of(LocalTime.of(10, 30), LocalTime.of(11, 0));
        Map<LocalDate, List<LocalTime>> dateAndTimeSlots = Map.of(date, times);

        Application application = Application.builder().build();

        //when
        application.updatePreferInterviewInfo(dateAndTimeSlots);

        //then
        Assertions.assertThat(application.getInterviewPreferences())
                .hasSize(1)
                .singleElement()
                .satisfies(pref -> {
                    Assertions.assertThat(pref.getDate()).isEqualTo(date);
                    Assertions.assertThat(pref.getTimes()).containsExactlyElementsOf(times);
                });
    }

}