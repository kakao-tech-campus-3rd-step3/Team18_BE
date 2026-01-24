package com.kakaotech.team18.backend_server.domain.application.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class InterviewPreference {

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private List<LocalTime> time = new ArrayList<>();
}