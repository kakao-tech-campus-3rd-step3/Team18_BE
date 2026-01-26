package com.kakaotech.team18.backend_server.domain.application.repository;

import java.time.LocalDate;
import java.time.LocalTime;

public interface InterviewSlotCountProjection {
    LocalDate getInterviewDate();
    LocalTime getInterviewTime();
    long getAssignedCount();
}