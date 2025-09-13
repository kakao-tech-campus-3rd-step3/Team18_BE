package com.kakaotech.team18.backend_server.domain.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationDetailResponseDto;
import com.kakaotech.team18.backend_server.domain.application.service.ApplicationService;
import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ApplicationNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collections;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationController.class)
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationService applicationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("지원서 상세 조회 컨트롤러 - 성공")
    void getApplicationDetail_success() throws Exception {
        // given
        Long clubId = 1L;
        Long applicantId = 1L;

        ApplicationDetailResponseDto.ApplicantInfo applicantInfo = new ApplicationDetailResponseDto.ApplicantInfo(
                applicantId, "김지원", "컴퓨터공학과", "20230001", "test@test.com", "010-1234-5678"
        );
        ApplicationDetailResponseDto responseDto = new ApplicationDetailResponseDto(
                100L, "PENDING", applicantInfo, Collections.emptyList()
        );

        given(applicationService.getApplicationDetail(clubId, applicantId)).willReturn(responseDto);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/clubs/{clubId}/applicants/{applicantId}/application", clubId, applicantId)
                        .accept(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationId").value(100L))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.applicantInfo.name").value("김지원"))
                .andExpect(jsonPath("$.applicantInfo.department").value("컴퓨터공학과"));
    }

    @Test
    @DisplayName("지원서 상세 조회 컨트롤러 - 실패 (지원서 없음)")
    void getApplicationDetail_fail_applicationNotFound() throws Exception {
        // given
        Long clubId = 1L;
        Long nonExistentApplicantId = 999L;

        given(applicationService.getApplicationDetail(clubId, nonExistentApplicantId))
                .willThrow(new ApplicationNotFoundException("detail message for test"));

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/clubs/{clubId}/applicants/{applicantId}/application", clubId, nonExistentApplicantId)
                        .accept(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error_code").value(ErrorCode.APPLICATION_NOT_FOUND.name()));
    }
}
