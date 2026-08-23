package com.kakaotech.team18.backend_server.domain.statistics.service;

import com.kakaotech.team18.backend_server.domain.statistics.dto.StatisticsResponseDto;
import com.kakaotech.team18.backend_server.domain.statistics.entity.StatisticsDimension;
import java.util.List;

public interface StatisticsService {

    /**
     * 동아리의 공개 통계를 조회합니다.
     * <p>
     * 동아리와 지원폼은 1:1이므로 {@code clubId}로 해당 지원폼을 찾아 집계한다. 프론트가 지원폼 ID를 따로 알지
     * 않아도 이미 가진 {@code clubId}로 호출할 수 있게 하기 위함이다.
     *
     * @param clubId     동아리 ID
     * @param dimensions 조회할 집계 항목
     * @return 공개 가능한 형태로 가공된 통계 응답
     */
    StatisticsResponseDto getStatistics(Long clubId, List<StatisticsDimension> dimensions);

    /**
     * 동아리의 관리자용 통계를 조회합니다.
     * <p>
     * 지원자를 직접 관리하는 동아리 관리자용이라 재식별 방지 마스킹·최소 공개 기준을 적용하지 않고 원본 수치를
     * 실시간으로 반환한다. 접근 권한은 컨트롤러의 인가 검사로 보장한다.
     *
     * @param clubId     동아리 ID
     * @param dimensions 조회할 집계 항목
     * @return 마스킹하지 않은 원본 통계 응답
     */
    StatisticsResponseDto getStatisticsForAdmin(Long clubId, List<StatisticsDimension> dimensions);
}
