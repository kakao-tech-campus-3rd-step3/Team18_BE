package com.kakaotech.team18.backend_server.domain.statistics.controller;

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

/**
 * 컨트롤러 슬라이스 단위 테스트. 보안은 {@link TestSecurityConfig}로 대체하고 서비스는 목킹한다.
 * 실제 {@code SecurityConfig}의 공개(permitAll) 규칙 검증은 {@code StatisticsControllerAuthTest}가 담당한다.
 */
@WebMvcTest(
        controllers = StatisticsController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
        }
)
@Import(TestSecurityConfig.class)
@DisplayName("StatisticsController 단위 테스트")
class StatisticsControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StatisticsService statisticsService;

    private StatisticsResponseDto sampleResponse() {
        return new StatisticsResponseDto(
                12L, 200L, false, OffsetDateTime.now(),
                List.of(new DimensionResult(
                        StatisticsDimension.GENDER, DimensionType.CATEGORICAL,
                        List.of(new Bucket("MALE", "남성", 121, new BigDecimal("0.605")))
                ))
        );
    }

    @Test
    @DisplayName("요청한 dimension이 서비스에 그대로 전달되고 응답이 직렬화된다")
    void getStatistics_passesRequestedDimensions() throws Exception {
        given(statisticsService.getStatistics(eq(12L), eq(List.of(StatisticsDimension.GENDER))))
                .willReturn(sampleResponse());

        mockMvc.perform(get("/api/club-apply-forms/{id}/statistics", 12L)
                        .param("dimensions", "GENDER")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clubApplyFormId").value(12))
                .andExpect(jsonPath("$.results[0].dimension").value("GENDER"))
                .andExpect(jsonPath("$.results[0].buckets[0].key").value("MALE"));
    }

    @Test
    @DisplayName("dimensions 생략 시 전체(defaults)로 서비스를 호출한다")
    void getStatistics_defaultsWhenOmitted() throws Exception {
        given(statisticsService.getStatistics(eq(12L), eq(StatisticsDimension.defaults())))
                .willReturn(sampleResponse());

        mockMvc.perform(get("/api/club-apply-forms/{id}/statistics", 12L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clubApplyFormId").value(12));
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
