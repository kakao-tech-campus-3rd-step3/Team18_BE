package com.kakaotech.team18.backend_server.domain.statistics.repository;

import com.kakaotech.team18.backend_server.domain.user.entity.Gender;

/** 성별 집계 결과 projection. */
public interface GenderCount {

    Gender getGender();

    long getCount();
}
