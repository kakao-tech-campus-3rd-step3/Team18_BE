package com.kakaotech.team18.backend_server.domain.FormQuestion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import java.time.LocalDate;

@Embeddable
public record TimeSlotOption(
        LocalDate date,
        @Embedded
        TimeRange availableTime
) {
    @Embeddable
    public record TimeRange(
            @Column(name = "start_time")
            String start,

            @Column(name = "end_time")
            String end
    ) {
    }
}