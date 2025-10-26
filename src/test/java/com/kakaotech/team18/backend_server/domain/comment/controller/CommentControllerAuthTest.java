package com.kakaotech.team18.backend_server.domain.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kakaotech.team18.backend_server.domain.application.repository.ApplicationRepository;
import com.kakaotech.team18.backend_server.domain.comment.service.CommentService;
import com.kakaotech.team18.backend_server.global.security.WithMockCustomUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class CommentControllerAuthTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService; // 컨트롤러가 의존하므로 Mock으로 유지

    @MockBean
    private ApplicationRepository applicationRepository; // CustomSecurityService가 의존하므로 Mock으로 추가

    @BeforeEach
    void setUp() {
        // @customSecurityService가 DB를 조회하는 부분을 Mocking합니다.
        // 100번 지원서는 1번 동아리에 속해있다고 가정합니다.
        given(applicationRepository.findClubIdByApplicationId(100L)).willReturn(Optional.of(1L));
        // 101번 지원서는 2번 동아리에 속해있다고 가정합니다.
        given(applicationRepository.findClubIdByApplicationId(101L)).willReturn(Optional.of(2L));
    }

    @DisplayName("댓글 조회 - 성공 (동아리 관리자)")
    @Test
    @WithMockCustomUser(memberships = {"1:CLUB_ADMIN"})
    void getComments_withAdminAuth_success() throws Exception {
        // given
        Long applicationId = 100L; // 1번 동아리에 속한 지원서

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/applications/{applicationId}/comments", applicationId)
        );

        // then
        // 사용자는 1번 동아리 관리자이고, 지원서도 1번 동아리 소속이므로 200 OK를 기대합니다.
        resultActions.andExpect(status().isOk());
    }

    @DisplayName("댓글 조회 - 실패 (권한 부족)")
    @Test
    @WithMockCustomUser(memberships = {"1:CLUB_MEMBER"})
    void getComments_withMemberAuth_fail() throws Exception {
        // given
        Long applicationId = 100L; // 1번 동아리에 속한 지원서

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/applications/{applicationId}/comments", applicationId)
        );

        // then
        // 사용자는 1번 동아리의 일반 회원이므로, 403 Forbidden을 기대합니다.
        resultActions.andExpect(status().isForbidden());
    }

    @DisplayName("댓글 조회 - 실패 (다른 동아리 관리자)")
    @Test
    @WithMockCustomUser(memberships = {"2:CLUB_ADMIN"})
    void getComments_withOtherClubAdminAuth_fail() throws Exception {
        // given
        Long applicationId = 100L; // 접근하려는 대상은 1번 동아리에 속한 지원서

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/applications/{applicationId}/comments", applicationId)
        );

        // then
        // 사용자는 2번 동아리 관리자이지만, 지원서는 1번 동아리 소속이므로 403 Forbidden을 기대합니다.
        resultActions.andExpect(status().isForbidden());
    }

    @DisplayName("댓글 조회 - 실패 (미인증 사용자)")
    @Test
    void getComments_withUnauthenticatedUser_fail() throws Exception {
        // given
        Long applicationId = 100L;

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/applications/{applicationId}/comments", applicationId)
        );

        // then
        // 인증되지 않은 사용자이므로 401 Unauthorized를 기대합니다.
        resultActions.andExpect(status().isUnauthorized());
    }
}