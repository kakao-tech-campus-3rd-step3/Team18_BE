package com.kakaotech.team18.backend_server.domain.statistics.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kakaotech.team18.backend_server.domain.statistics.dto.StatisticsResponseDto;
import com.kakaotech.team18.backend_server.domain.statistics.entity.StatisticsDimension;
import com.kakaotech.team18.backend_server.domain.statistics.service.StatisticsService;
import com.kakaotech.team18.backend_server.global.config.SecurityConfig;
import com.kakaotech.team18.backend_server.global.config.TestSecurityConfig;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ClubApplyFormNotFoundException;
import com.kakaotech.team18.backend_server.global.security.JwtAuthenticationFilter;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
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
@DisplayName("지원자 통계 API")
class StatisticsControllerTest {

    private static final long FORM_ID = 12L;
    private static final String URL = "/api/club-apply-forms/" + FORM_ID + "/statistics";

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    StatisticsService statisticsService;

    private StatisticsResponseDto sampleResponse() {
        return new StatisticsResponseDto(FORM_ID, 214, false, OffsetDateTime.now(), List.of(
                new StatisticsResponseDto.DimensionResult(
                        StatisticsDimension.GENDER, StatisticsDimension.GENDER.getType(), null, null,
                        List.of(
                                new StatisticsResponseDto.Bucket(
                                        "MALE", "남성", 121, new BigDecimal("0.565"), null),
                                new StatisticsResponseDto.Bucket(
                                        "FEMALE", "여성", 91, new BigDecimal("0.425"), null)))
        ));
    }

    @DisplayName("통계 조회 성공 - 비로그인 상태에서도 200을 반환한다")
    @Test
    void getStatistics_success() throws Exception {
        when(statisticsService.getStatistics(anyLong(), any())).thenReturn(sampleResponse());

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clubApplyFormId").value(FORM_ID))
                .andExpect(jsonPath("$.totalApplicants").value(214))
                .andExpect(jsonPath("$.snapshot").value(false))
                .andExpect(jsonPath("$.results[0].dimension").value("GENDER"))
                .andExpect(jsonPath("$.results[0].type").value("CATEGORICAL"))
                .andExpect(jsonPath("$.results[0].buckets[0].key").value("MALE"))
                .andExpect(jsonPath("$.results[0].buckets[0].label").value("남성"))
                .andExpect(jsonPath("$.results[0].buckets[0].ratio").value(0.565));
    }

    @DisplayName("dimension을 지정하지 않으면 전체 항목을 조회한다")
    @Test
    void getStatistics_defaultsToAllDimensions() throws Exception {
        when(statisticsService.getStatistics(anyLong(), any())).thenReturn(sampleResponse());

        mockMvc.perform(get(URL)).andExpect(status().isOk());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<StatisticsDimension>> captor = ArgumentCaptor.forClass(List.class);
        verify(statisticsService).getStatistics(anyLong(), captor.capture());

        org.assertj.core.api.Assertions.assertThat(captor.getValue())
                .containsExactlyElementsOf(StatisticsDimension.defaults());
    }

    @DisplayName("요청한 dimension만 서비스로 전달한다")
    @Test
    void getStatistics_passesRequestedDimensions() throws Exception {
        when(statisticsService.getStatistics(anyLong(), any())).thenReturn(sampleResponse());

        mockMvc.perform(get(URL).param("dimensions", "GENDER", "DEPARTMENT"))
                .andExpect(status().isOk());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<StatisticsDimension>> captor = ArgumentCaptor.forClass(List.class);
        verify(statisticsService).getStatistics(anyLong(), captor.capture());

        org.assertj.core.api.Assertions.assertThat(captor.getValue())
                .containsExactly(StatisticsDimension.GENDER, StatisticsDimension.DEPARTMENT);
    }

    @DisplayName("소문자 dimension도 허용한다")
    @Test
    void getStatistics_acceptsLowerCaseDimension() throws Exception {
        when(statisticsService.getStatistics(anyLong(), any())).thenReturn(sampleResponse());

        mockMvc.perform(get(URL).param("dimensions", "gender"))
                .andExpect(status().isOk());
    }

    @DisplayName("지원하지 않는 dimension은 400으로 처리한다")
    @Test
    void getStatistics_unsupportedDimension() throws Exception {
        mockMvc.perform(get(URL).param("dimensions", "AGE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_code").value("UNSUPPORTED_STATISTICS_DIMENSION"));

        // 잘못된 요청이 서비스까지 내려가지 않는다.
        verify(statisticsService, never()).getStatistics(anyLong(), any());
    }

    @DisplayName("존재하지 않는 지원폼은 404로 처리한다")
    @Test
    void getStatistics_formNotFound() throws Exception {
        when(statisticsService.getStatistics(anyLong(), any()))
                .thenThrow(new ClubApplyFormNotFoundException("clubApplyFormId = " + FORM_ID));

        mockMvc.perform(get(URL))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error_code").value("FORM_NOT_FOUND"));
    }

    @DisplayName("응답에 지원자 식별 정보가 포함되지 않는다")
    @Test
    void getStatistics_containsNoIdentifyingInformation() throws Exception {
        when(statisticsService.getStatistics(anyLong(), any())).thenReturn(sampleResponse());

        String body = mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        org.assertj.core.api.Assertions.assertThat(body)
                .doesNotContain("studentId")
                .doesNotContain("email")
                .doesNotContain("phoneNumber")
                .doesNotContain("applicationId");
    }
}
