package com.kakaotech.team18.backend_server.domain.statistics.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kakaotech.team18.backend_server.domain.statistics.dto.StatisticsResponseDto;
import com.kakaotech.team18.backend_server.domain.statistics.service.StatisticsService;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 실제 {@code SecurityConfig} 하에서 공개(permitAll) 규칙을 검증하는 통합 테스트.
 * <p>
 * 단위 슬라이스({@code StatisticsControllerUnitTest})는 {@code TestSecurityConfig}로 보안을 대체하므로
 * "비로그인 조회 허용"을 검증하지 못한다. 여기서는 {@code @Import(TestSecurityConfig)} 없이 운영 보안 체인을
 * 그대로 태워, 인증 없이도 통계 조회가 200으로 열려 있는지 확인한다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class StatisticsControllerAuthTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StatisticsService statisticsService;

    @Test
    @DisplayName("통계 조회는 비로그인 상태에서도 200 (permitAll)")
    void getStatistics_isPublic() throws Exception {
        given(statisticsService.getStatistics(any(), any()))
                .willReturn(new StatisticsResponseDto(12L, 0L, false, false, OffsetDateTime.now(), List.of()));

        mockMvc.perform(get("/api/club-apply-forms/{id}/statistics", 12L))
                .andExpect(status().isOk());
    }
}
