package com.kakaotech.team18.backend_server.domain.statistics.repository;

/**
 * 학번별 지원자 수 집계 결과 projection.
 * <p>
 * 입학연도로의 변환은 세기 판정이 필요해 애플리케이션에서 수행한다.
 */
public interface StudentIdCount {

    String getStudentId();

    long getCount();
}
