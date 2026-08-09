package com.kakaotech.team18.backend_server.domain.statistics.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kakaotech.team18.backend_server.domain.clubApplyForm.repository.ClubApplyFormRepository;
import com.kakaotech.team18.backend_server.domain.statistics.dto.StatisticsResponseDto;
import com.kakaotech.team18.backend_server.domain.statistics.service.StatisticsService;
import com.kakaotech.team18.backend_server.global.security.WithMockCustomUser;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
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
 * 인증이 필요하며, 세부 인가는 {@code @customSecurityService.isClubAdminOrExecutiveForApplyForm}가 담당한다.
 * DB 조회(지원폼 → 동아리 매핑)는 {@link ClubApplyFormRepository}를 목킹해 대체한다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test") // DataInitializer(@Profile prod/default) 비활성화 → 리포지토리 목킹이 시드와 충돌하지 않게 한다
@DisplayName("StatisticsAdminController 인가 테스트")
class StatisticsAdminControllerAuthTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StatisticsService statisticsService;

    @MockitoBean
    private ClubApplyFormRepository clubApplyFormRepository; // CustomSecurityService의 인가 조회를 대체

    @BeforeEach
    void setUp() {
        // 12번 지원폼은 1번 동아리 소속이라고 가정한다.
        given(clubApplyFormRepository.findClubIdByClubApplyFormId(12L)).willReturn(Optional.of(1L));
        given(statisticsService.getStatisticsForAdmin(any(), any()))
                .willReturn(new StatisticsResponseDto(12L, 0L, false, OffsetDateTime.now(), List.of()));
    }

    @Test
    @DisplayName("동아리 관리자 → 200")
    @WithMockCustomUser(memberships = {"1:CLUB_ADMIN"})
    void getStatisticsForAdmin_withClubAdmin_ok() throws Exception {
        mockMvc.perform(get("/api/club-apply-forms/{id}/statistics/admin", 12L))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("동아리 운영진 → 200")
    @WithMockCustomUser(memberships = {"1:CLUB_EXECUTIVE"})
    void getStatisticsForAdmin_withClubExecutive_ok() throws Exception {
        mockMvc.perform(get("/api/club-apply-forms/{id}/statistics/admin", 12L))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("동아리 일반 회원 → 403")
    @WithMockCustomUser(memberships = {"1:CLUB_MEMBER"})
    void getStatisticsForAdmin_withClubMember_forbidden() throws Exception {
        mockMvc.perform(get("/api/club-apply-forms/{id}/statistics/admin", 12L))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("다른 동아리 관리자 → 403")
    @WithMockCustomUser(memberships = {"2:CLUB_ADMIN"})
    void getStatisticsForAdmin_withOtherClubAdmin_forbidden() throws Exception {
        mockMvc.perform(get("/api/club-apply-forms/{id}/statistics/admin", 12L))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("미인증 사용자 → 401")
    void getStatisticsForAdmin_withUnauthenticatedUser_unauthorized() throws Exception {
        mockMvc.perform(get("/api/club-apply-forms/{id}/statistics/admin", 12L))
                .andExpect(status().isUnauthorized());
    }
}
