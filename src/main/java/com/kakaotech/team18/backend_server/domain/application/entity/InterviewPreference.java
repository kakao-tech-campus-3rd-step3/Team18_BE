package com.kakaotech.team18.backend_server.domain.application.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
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

    @ElementCollection
    @CollectionTable(
            name = "interview_preference_time",
            joinColumns = {
                    @JoinColumn(name = "application_id"),
                    @JoinColumn(name = "pref_idx")
            }
    )
    @Column(name = "time", nullable = false)
    private List<LocalTime> time = new ArrayList<>();
}