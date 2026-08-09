package com.kakaotech.team18.backend_server.domain.statistics.repository;

import com.kakaotech.team18.backend_server.domain.user.entity.Faculty;

/** 학부 집계 결과 projection. */
public interface FacultyCount {

    Faculty getFaculty();

    long getCount();
}
