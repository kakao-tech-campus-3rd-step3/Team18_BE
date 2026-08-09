package com.kakaotech.team18.backend_server.domain.statistics.controller;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kakaotech.team18.backend_server.domain.statistics.dto.StatisticsResponseDto;
import com.kakaotech.team18.backend_server.domain.statistics.dto.StatisticsResponseDto.Bucket;
import com.kakaotech.team18.backend_server.domain.statistics.dto.StatisticsResponseDto.DimensionResult;
import com.kakaotech.team18.backend_server.domain.statistics.entity.DimensionType;
import com.kakaotech.team18.backend_server.domain.statistics.entity.StatisticsDimension;
import com.kakaotech.team18.backend_server.domain.statistics.service.StatisticsService;
import com.kakaotech.team18.backend_server.global.config.SecurityConfig;
import com.kakaotech.team18.backend_server.global.config.TestSecurityConfig;
import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;
import com.kakaotech.team18.backend_server.global.security.JwtAuthenticationFilter;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = StatisticsController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
        }
)
@Import(TestSecurityConfig.class)
@DisplayName("StatisticsController - 통계 조회 API")
class StatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StatisticsService statisticsService;

    private StatisticsResponseDto sampleResponse() {
        return new StatisticsResponseDto(
                12L, 200L, false, OffsetDateTime.now(),
                List.of(new DimensionResult(
                        StatisticsDimension.GENDER, DimensionType.CATEGORICAL, null, null,
                        List.of(new Bucket("MALE", "남성", 121, new BigDecimal("0.605"), null))
                ))
        );
    }

    @Test
    @DisplayName("유효한 dimension 요청 → 200과 통계 응답")
    void getStatistics_ok() throws Exception {
        given(statisticsService.getStatistics(eq(12L), anyList())).willReturn(sampleResponse());

        mockMvc.perform(get("/api/club-apply-forms/{id}/statistics", 12L)
                        .param("dimensions", "GENDER")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clubApplyFormId").value(12))
                .andExpect(jsonPath("$.results[0].dimension").value("GENDER"))
                .andExpect(jsonPath("$.results[0].buckets[0].key").value("MALE"));
    }

    @Test
    @DisplayName("dimensions 생략 → 200 (전체 조회)")
    void getStatistics_defaultDimensions_ok() throws Exception {
        given(statisticsService.getStatistics(eq(12L), anyList())).willReturn(sampleResponse());

        mockMvc.perform(get("/api/club-apply-forms/{id}/statistics", 12L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("지원하지 않는 dimension → 400")
    void getStatistics_unsupportedDimension_badRequest() throws Exception {
        mockMvc.perform(get("/api/club-apply-forms/{id}/statistics", 12L)
                        .param("dimensions", "NOT_A_DIMENSION")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_code").value(ErrorCode.UNSUPPORTED_STATISTICS_DIMENSION.name()));
    }
}
