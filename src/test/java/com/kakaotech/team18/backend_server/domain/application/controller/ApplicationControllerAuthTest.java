package com.kakaotech.team18.backend_server.domain.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kakaotech.team18.backend_server.domain.application.service.ApplicationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class ApplicationControllerAuthTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApplicationService applicationService; // 서비스 로직은 Mocking 합니다.

    @DisplayName("지원서 상세 조회 - 성공 (동아리 관리자)")
    @Test
    @WithMockUser(authorities = "CLUB_1_CLUB_ADMIN")
    void getApplicationDetail_withAdminAuth_success() throws Exception {
        // given
        Long clubId = 1L;
        Long applicantId = 12L;

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/clubs/{clubId}/applicants/{applicantId}/application", clubId, applicantId)
        );

        // then
        resultActions.andExpect(status().isOk());
    }

    @DisplayName("지원서 상세 조회 - 실패 (권한 부족)")
    @Test
    @WithMockUser(authorities = "CLUB_1_CLUB_MEMBER")
    void getApplicationDetail_withMemberAuth_fail() throws Exception {
        // given
        Long clubId = 1L;
        Long applicantId = 12L;

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/clubs/{clubId}/applicants/{applicantId}/application", clubId, applicantId)
        );

        // then
        resultActions.andExpect(status().isForbidden());
    }

    @DisplayName("지원서 상세 조회 - 실패 (다른 동아리 관리자)")
    @Test
    @WithMockUser(authorities = "CLUB_2_CLUB_ADMIN")
    void getApplicationDetail_withOtherClubAdminAuth_fail() throws Exception {
        // given
        Long targetClubId = 1L;
        Long applicantId = 12L;

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/clubs/{clubId}/applicants/{applicantId}/application", targetClubId, applicantId)
        );

        // then
        resultActions.andExpect(status().isForbidden());
    }

    @DisplayName("지원서 상세 조회 - 실패 (미인증 사용자)")
    @Test
    void getApplicationDetail_withUnauthenticatedUser_fail() throws Exception {
        // given
        Long clubId = 1L;
        Long applicantId = 12L;

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/clubs/{clubId}/applicants/{applicantId}/application", clubId, applicantId)
        );

        // then
        resultActions.andExpect(status().isUnauthorized());
    }
}