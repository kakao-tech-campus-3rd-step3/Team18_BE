package com.kakaotech.team18.backend_server.domain.statistics.service;

import com.kakaotech.team18.backend_server.domain.statistics.dto.StatisticsResponseDto;
import com.kakaotech.team18.backend_server.domain.statistics.entity.StatisticsDimension;
import java.util.List;

public interface StatisticsService {

    /**
     * 지원폼의 공개 통계를 조회합니다.
     *
     * @param clubApplyFormId 지원폼 ID
     * @param dimensions      조회할 집계 항목
     * @return 공개 가능한 형태로 가공된 통계 응답
     */
    StatisticsResponseDto getStatistics(Long clubApplyFormId, List<StatisticsDimension> dimensions);
}
