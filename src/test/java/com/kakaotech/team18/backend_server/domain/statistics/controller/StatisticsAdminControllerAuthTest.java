package com.kakaotech.team18.backend_server.domain.statistics.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kakaotech.team18.backend_server.domain.statistics.dto.StatisticsResponseDto;
import com.kakaotech.team18.backend_server.domain.statistics.service.StatisticsService;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ClubApplyFormNotFoundException;
import com.kakaotech.team18.backend_server.global.security.WithMockCustomUser;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 실제 {@code SecurityConfig}·메서드 시큐리티 하에서 관리자 통계 API의 인가를 검증하는 통합 테스트.
 * <p>
 * 공개 통계({@code /statistics})와 달리 이 경로({@code /statistics/admin})는 permitAll 매처에 걸리지 않아
 * 인증이 필요하며, 세부 인가는 {@code @customSecurityService.isClubAdminOrExecutive(#clubId)}가 담당한다.
 * 인가는 JWT 멤버십만 읽으므로 DB 조회가 없다(경로 파라미터가 곧 clubId).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test") // DataInitializer(@Profile prod/default) 비활성화
@DisplayName("StatisticsAdminController 인가 테스트")
class StatisticsAdminControllerAuthTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StatisticsService statisticsService;

    @BeforeEach
    void setUp() {
        given(statisticsService.getStatisticsForAdmin(any(), any()))
                .willReturn(new StatisticsResponseDto(12L, 0L, false, false, OffsetDateTime.now(), List.of()));
    }

    @Test
    @DisplayName("동아리 관리자 → 200")
    @WithMockCustomUser(memberships = {"1:CLUB_ADMIN"})
    void getStatisticsForAdmin_withClubAdmin_ok() throws Exception {
        mockMvc.perform(get("/api/clubs/{clubId}/statistics/admin", 1L))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("동아리 운영진 → 200")
    @WithMockCustomUser(memberships = {"1:CLUB_EXECUTIVE"})
    void getStatisticsForAdmin_withClubExecutive_ok() throws Exception {
        mockMvc.perform(get("/api/clubs/{clubId}/statistics/admin", 1L))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("동아리 일반 회원 → 403")
    @WithMockCustomUser(memberships = {"1:CLUB_MEMBER"})
    void getStatisticsForAdmin_withClubMember_forbidden() throws Exception {
        mockMvc.perform(get("/api/clubs/{clubId}/statistics/admin", 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("다른 동아리 관리자 → 403 (경로 clubId와 멤버십 clubId 불일치)")
    @WithMockCustomUser(memberships = {"2:CLUB_ADMIN"})
    void getStatisticsForAdmin_withOtherClubAdmin_forbidden() throws Exception {
        mockMvc.perform(get("/api/clubs/{clubId}/statistics/admin", 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("미인증 사용자 → 401")
    void getStatisticsForAdmin_withUnauthenticatedUser_unauthorized() throws Exception {
        mockMvc.perform(get("/api/clubs/{clubId}/statistics/admin", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("관리자지만 해당 동아리의 지원폼이 없으면 → 404 (서비스에서 전파)")
    @WithMockCustomUser(memberships = {"1:CLUB_ADMIN"})
    void getStatisticsForAdmin_missingForm_notFound() throws Exception {
        // 인가는 통과(1번 동아리 관리자)하나, 서비스가 지원폼을 못 찾아 404를 던진다.
        given(statisticsService.getStatisticsForAdmin(eq(1L), any()))
                .willThrow(new ClubApplyFormNotFoundException("clubId = 1"));

        mockMvc.perform(get("/api/clubs/{clubId}/statistics/admin", 1L))
                .andExpect(status().isNotFound());
    }
}
