package com.kakaotech.team18.backend_server.domain.statistics.entity;

/**
 * 통계 dimension의 데이터 성격.
 * <p>
 * 백엔드는 버킷과 이 타입만 제공하고, 어떤 차트로 그릴지는 프론트엔드가 정한다.
 */
public enum DimensionType {

    /** 순서가 없는 범주형. 보통 파이/도넛으로 표현한다. (성별, 학과) */
    CATEGORICAL,

    /** 순서가 있는 범주형. 버킷 순서를 그대로 유지해야 한다. (학번) */
    ORDINAL,

    /** 시간축 위의 값. 버킷 순서를 그대로 유지해야 한다. (일자별 지원 추이) */
    TIME_SERIES
}
