package com.kakaotech.team18.backend_server.domain.clubApplyForm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.dto.ClubApplyFormRequestDto;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.service.ClubApplyFormService;
import com.kakaotech.team18.backend_server.domain.formQuestion.dto.FormQuestionRequestDto;
import com.kakaotech.team18.backend_server.domain.formQuestion.dto.FormQuestionResponseDto;
import com.kakaotech.team18.backend_server.domain.formQuestion.entity.FieldType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class ClubApplyFormControllerAuthTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClubApplyFormService clubApplyFormService;

    @DisplayName("지원서 양식 생성 - 성공 (동아리 관리자)")
    @Test
    @WithMockUser(authorities = "CLUB_1_CLUB_ADMIN")
    void createClubApplyForm_withAdminAuth_success() throws Exception {
        // given
        Long clubId = 1L;
        ClubApplyFormRequestDto requestDto = new ClubApplyFormRequestDto(
                "Dummy Title",
                "Dummy Description",
                List.of(new FormQuestionRequestDto("질문 1", FieldType.TEXT, true, 1L, null, null))
        );

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/clubs/{clubId}/dashboard/apply-form", clubId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
        );

        // then
        // 리소스가 성공적으로 생성되었으므로 201 Created 응답을 기대합니다.
        resultActions.andExpect(status().isCreated());
    }

    @DisplayName("지원서 양식 생성 - 실패 (권한 부족)")
    @Test
    @WithMockUser(authorities = "CLUB_1_CLUB_MEMBER")
    void createClubApplyForm_withMemberAuth_fail() throws Exception {
        // given
        Long clubId = 1L;
        ClubApplyFormRequestDto requestDto = new ClubApplyFormRequestDto(
                "Dummy Title",
                "Dummy Description",
                List.of(new FormQuestionRequestDto("질문 1", FieldType.TEXT, true, 1L, null, null))
        );

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/clubs/{clubId}/dashboard/apply-form", clubId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
        );

        // then
        resultActions.andExpect(status().isForbidden());
    }

    @DisplayName("지원서 양식 생성 - 실패 (다른 동아리 관리자)")
    @Test
    @WithMockUser(authorities = "CLUB_2_CLUB_ADMIN")
    void createClubApplyForm_withOtherClubAdminAuth_fail() throws Exception {
        // given
        Long targetClubId = 1L;
        ClubApplyFormRequestDto requestDto = new ClubApplyFormRequestDto(
                "Dummy Title",
                "Dummy Description",
                List.of(new FormQuestionRequestDto("질문 1", FieldType.TEXT, true, 1L, null, null))
        );

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/clubs/{clubId}/dashboard/apply-form", targetClubId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
        );

        // then
        resultActions.andExpect(status().isForbidden());
    }

    @DisplayName("지원서 양식 생성 - 실패 (미인증 사용자)")
    @Test
    void createClubApplyForm_withUnauthenticatedUser_fail() throws Exception {
        // given
        Long clubId = 1L;
        ClubApplyFormRequestDto requestDto = new ClubApplyFormRequestDto(
                "Dummy Title",
                "Dummy Description",
                List.of(new FormQuestionRequestDto("질문 1", FieldType.TEXT, true, 1L, null, null))
        );

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/clubs/{clubId}/dashboard/apply-form", clubId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
        );

        // then
        resultActions.andExpect(status().isUnauthorized());
    }
}