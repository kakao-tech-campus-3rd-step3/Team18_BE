package com.kakaotech.team18.backend_server.domain.statistics.cache;

import com.kakaotech.team18.backend_server.domain.statistics.dto.StatisticsResponseDto;
import java.util.Optional;

/**
 * 공개 통계 결과 캐시.
 * <p>
 * 지원폼 단위로 <strong>마스킹하지 않은 전체 dimension 결과</strong>를 캐시한다. 마스킹·dimension subset은
 * 조회 시점(serve time)에 적용하므로, 캐시에는 원본 계산 결과만 담는다.
 * <p>
 * 캐시 장애가 조회를 막아서는 안 된다. 구현체는 조회/저장 실패 시 예외를 던지지 말고 각각 '미스'와 '무시'로
 * 처리해, 호출부가 실시간 계산으로 자연스럽게 폴백하도록 한다.
 */
public interface StatisticsCache {

    /**
     * 지원폼의 캐시된 전체 통계를 조회합니다. 없거나 캐시 장애면 빈 값을 반환합니다.
     */
    Optional<StatisticsResponseDto> find(Long clubApplyFormId);

    /**
     * 지원폼의 전체 통계를 캐시에 저장합니다. 저장에 실패해도 예외를 던지지 않습니다.
     */
    void put(Long clubApplyFormId, StatisticsResponseDto fullStatistics);
}
