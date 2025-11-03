package com.kakaotech.team18.backend_server.domain.formQuestion.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import java.time.LocalTime;

@Embeddable
public record TimeSlotOption(
        @Schema(description = "날짜 (YYYY-MM-DD ~ YYYY-MM-DD)", example = "2024-10-01 ~ 2024-10-27")
        String date,
        @Embedded
        TimeRange availableTime
) {
    @Embeddable
    public record TimeRange(
            @Column(name = "start_time")
            LocalTime start,

            @Column(name = "end_time")
            LocalTime end
    ) {
    }
}