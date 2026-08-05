package com.kakaotech.team18.backend_server.domain.clubPopularity.controller;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.Mockito.when;

import com.kakaotech.team18.backend_server.domain.clubPopularity.service.ClubPopularityRecordingService;
import com.kakaotech.team18.backend_server.domain.clubPopularity.service.ClubPopularityQueryService;
import com.kakaotech.team18.backend_server.global.config.SecurityConfig;
import com.kakaotech.team18.backend_server.global.config.TestSecurityConfig;
import com.kakaotech.team18.backend_server.global.security.ActivityTrackingFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;

@WebMvcTest(
        controllers = ClubPopularityController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@Import(TestSecurityConfig.class)
class ClubPopularityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClubPopularityRecordingService recordingService;

    @MockitoBean
    private ClubPopularityQueryService queryService;

    @Test
    @DisplayName("views는 본문 없이 204를 반환하고 서비스에 식별자를 전달한다")
    void recordsViewWithoutRequestBody() throws Exception {
        mockMvc.perform(post("/api/clubs/{clubId}/views", 7L)
                        .requestAttr(ActivityTrackingFilter.ANONYMOUS_ID_REQUEST_ATTRIBUTE, "anonymous-id"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(recordingService).recordView(eq(7L), isNull(Authentication.class), eq("anonymous-id"));
    }

    @Test
    @DisplayName("heartbeat는 본문 없이 204를 반환한다")
    void recordsHeartbeatWithoutRequestBody() throws Exception {
        mockMvc.perform(post("/api/clubs/{clubId}/heartbeat", 7L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(recordingService).recordHeartbeat(eq(7L), isNull(Authentication.class), isNull(String.class));
    }

    @ParameterizedTest
    @EnumSource(value = ClubPopularityRecordingService.RecordingResult.class,
            names = {"REDIS_ERROR", "DISABLED", "INVALID_IDENTITY"})
    void recordingOutcomesStillReturnNoContent(ClubPopularityRecordingService.RecordingResult result) throws Exception {
        when(recordingService.recordView(eq(7L), isNull(Authentication.class), eq("anonymous-id")))
                .thenReturn(result);
        when(recordingService.recordHeartbeat(eq(7L), isNull(Authentication.class), isNull(String.class)))
                .thenReturn(result);

        mockMvc.perform(post("/api/clubs/{clubId}/views", 7L)
                        .requestAttr(ActivityTrackingFilter.ANONYMOUS_ID_REQUEST_ATTRIBUTE, "anonymous-id"))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/clubs/{clubId}/heartbeat", 7L))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("popular은 조회 서비스 결과를 200으로 반환한다")
    void getsPopularClubs() throws Exception {
        when(queryService.getPopularClubs()).thenReturn(
                new com.kakaotech.team18.backend_server.domain.clubPopularity.dto.ClubPopularityResponse(
                        java.util.List.of(new com.kakaotech.team18.backend_server.domain.clubPopularity.dto.PopularClubResponse(
                                7L, 10, 3, true, true))));

        mockMvc.perform(get("/api/clubs/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clubs[0].clubId").value(7))
                .andExpect(jsonPath("$.clubs[0].recentViewerBadge").value(true));
    }
}
