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

    /**
     * 지원폼의 관리자용 통계를 조회합니다.
     * <p>
     * 지원자를 직접 관리하는 동아리 관리자용이라 재식별 방지 마스킹·최소 공개 기준·마감 blackout을 적용하지 않고
     * 원본 수치를 실시간으로 반환합니다. 접근 권한은 컨트롤러의 인가 검사로 보장합니다.
     *
     * @param clubApplyFormId 지원폼 ID
     * @param dimensions      조회할 집계 항목
     * @return 마스킹하지 않은 원본 통계 응답
     */
    StatisticsResponseDto getStatisticsForAdmin(Long clubApplyFormId, List<StatisticsDimension> dimensions);
}
