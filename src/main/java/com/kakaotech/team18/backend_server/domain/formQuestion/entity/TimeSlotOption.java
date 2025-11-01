package com.kakaotech.team18.backend_server.domain.formQuestion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import java.time.LocalTime;

@Embeddable
public record TimeSlotOption(
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