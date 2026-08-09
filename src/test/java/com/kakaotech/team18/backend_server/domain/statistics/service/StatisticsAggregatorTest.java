package com.kakaotech.team18.backend_server.domain.statistics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.domain.statistics.entity.StatisticsDimension;
import com.kakaotech.team18.backend_server.domain.statistics.repository.ApplicationStatisticsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("StatisticsAggregator - 계약 골격")
class StatisticsAggregatorTest {

    private final ApplicationStatisticsRepository repository = mock(ApplicationStatisticsRepository.class);
    private final StatisticsAggregator aggregator = new StatisticsAggregator(repository);

    @Test
    @DisplayName("countApplicants는 리포지토리 count에 위임한다")
    void countApplicants_delegates() {
        when(repository.countByClubApplyFormId(1L)).thenReturn(42L);

        assertThat(aggregator.countApplicants(1L)).isEqualTo(42L);
    }

    @Test
    @DisplayName("아직 어떤 dimension도 구현되지 않아 모두 빈 버킷을 반환한다")
    void aggregate_allDimensionsEmpty() {
        ClubApplyForm form = mock(ClubApplyForm.class);

        for (StatisticsDimension dimension : StatisticsDimension.values()) {
            assertThat(aggregator.aggregate(form, dimension))
                    .as("dimension %s", dimension)
                    .isEmpty();
        }
    }
}
