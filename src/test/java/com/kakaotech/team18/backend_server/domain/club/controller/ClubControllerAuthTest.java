package com.kakaotech.team18.backend_server.domain.club.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kakaotech.team18.backend_server.domain.club.service.ClubService;
import com.kakaotech.team18.backend_server.global.security.CustomSecurityService;
import com.kakaotech.team18.backend_server.global.security.JwtProvider;
import com.kakaotech.team18.backend_server.global.security.PrincipalDetailsService;
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
// @Import(TestSecurityConfig.class) // 실제 SecurityConfig를 사용하기 위해 이 줄을 추가하지 않습니다.
class ClubControllerAuthTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClubService clubService; // 서비스 로직은 Mocking 합니다.

    @DisplayName("동아리 대시보드 조회 - 성공 (동아리 관리자)")
    @Test
    @WithMockUser(authorities = "CLUB_1_CLUB_ADMIN") // 1번 동아리의 관리자 권한을 가진 가짜 사용자
    void getClubDashboard_withAdminAuth_success() throws Exception {
        // given
        Long clubId = 1L;

        // 서비스 계층의 동작은 Mocking 처리합니다.

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/clubs/{clubId}/dashboard", clubId)
        );

        // then
        // 인가에 성공했으므로 200 OK 응답을 기대합니다.
        resultActions.andExpect(status().isOk());
    }

    @DisplayName("동아리 대시보드 조회 - 실패 (권한 부족)")
    @Test
    @WithMockUser(authorities = "CLUB_1_CLUB_MEMBER") // 1번 동아리의 '일반 회원' 권한을 가진 가짜 사용자
    void getClubDashboard_withMemberAuth_fail() throws Exception {
        // given
        Long clubId = 1L;

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/clubs/{clubId}/dashboard", clubId)
        );

        // then
        // 인가에 실패했으므로 403 Forbidden 응답을 기대합니다.
        resultActions.andExpect(status().isForbidden());
    }

}