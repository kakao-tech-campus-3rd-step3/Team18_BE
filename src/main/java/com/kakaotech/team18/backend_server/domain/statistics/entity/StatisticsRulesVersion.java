package com.kakaotech.team18.backend_server.domain.statistics.entity;

/**
 * 집계 규칙 버전.
 * <p>
 * 마스킹 임계값의 의미, 상위 N 절단 방식, 버킷 key 체계처럼 <strong>같은 원본 데이터에서 다른 결과가 나오게
 * 만드는 변경</strong>이 생기면 이 값을 올린다. 스냅샷에 함께 저장해 두면 나중에 "이 수치가 어떤 규칙으로
 * 계산된 것인지" 판별할 수 있고, 규칙이 바뀌었을 때 재계산 대상을 골라낼 수 있다.
 * <p>
 * 캐시 키에도 같은 버전을 쓴다. 버전을 올리면 예전 규칙으로 계산된 캐시 엔트리는 자연히 무시된다.
 */
public final class StatisticsRulesVersion {

    public static final String CURRENT = "v1";

    private StatisticsRulesVersion() {
    }
}
