package com.kakaotech.team18.backend_server.domain.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationApplyRequestDto;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationApplyResponseDto;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationDetailResponseDto;
import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationStatusUpdateRequestDto;
import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import com.kakaotech.team18.backend_server.domain.application.service.ApplicationService;
import com.kakaotech.team18.backend_server.global.config.TestSecurityConfig;
import com.kakaotech.team18.backend_server.global.dto.SuccessResponseDto;
import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ApplicationNotFoundException;
import com.kakaotech.team18.backend_server.global.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collections;

import static java.time.LocalDateTime.now;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ApplicationController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@Import(TestSecurityConfig.class)
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
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

    @Test
    @DisplayName("지원서 상태 변경 컨트롤러 - 성공")
    void updateApplicationStatus_success() throws Exception {
        // given
        Long applicationId = 1L;
        ApplicationStatusUpdateRequestDto requestDto = new ApplicationStatusUpdateRequestDto(Status.APPROVED);
        given(applicationService.updateApplicationStatus(any(Long.class), any(ApplicationStatusUpdateRequestDto.class)))
                .willReturn(new SuccessResponseDto(true));

        // when
        ResultActions resultActions = mockMvc.perform(
                patch("/api/applications/{applicationId}", applicationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("지원서 상태 변경 컨트롤러 - 실패 (지원서 없음)")
    void updateApplicationStatus_fail_applicationNotFound() throws Exception {
        // given
        Long nonExistentApplicationId = 999L;
        ApplicationStatusUpdateRequestDto requestDto = new ApplicationStatusUpdateRequestDto(Status.APPROVED);
        given(applicationService.updateApplicationStatus(any(Long.class), any(ApplicationStatusUpdateRequestDto.class)))
                .willThrow(new ApplicationNotFoundException("테스트 detail 블라블라"));

        // when
        ResultActions resultActions = mockMvc.perform(
                patch("/api/applications/{applicationId}", nonExistentApplicationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
        );

        // then
        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error_code").value(ErrorCode.APPLICATION_NOT_FOUND.name()));
    }

    @Test
    @DisplayName("지원서 상태 변경 컨트롤러 - 실패 (잘못된 요청 값)")
    void updateApplicationStatus_fail_invalidInputValue() throws Exception {
        // given
        Long applicationId = 1L;
        // status 필드가 없는 잘못된 요청
        String invalidRequestBody = "{}";

        // when
        ResultActions resultActions = mockMvc.perform(
                patch("/api/applications/{applicationId}", applicationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody)
        );

        // then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_code").value(ErrorCode.INVALID_INPUT_VALUE.name()));
    }

    @Nested
    @DisplayName("POST /api/clubs/{clubId}/apply-submit")
    class SubmitApplicationEndpoint {

        private String validPayloadJson() {
            return """
        {
          "studentId":"20231234",
          "email":"stud@example.com",
          "name":"홍길동",
          "phoneNumber":"010-0000-0000",
          "department":"컴퓨터공학과",
          "answerList": [
          {"questionId":1,"answerContent":"자기소개입니다"},
          {"questionId":2,"answerContent":"여"},
          {"questionId":3,"answerContent":"A,B"}
          ]
        }
        """;
        }

        private String invalidPayloadJson() {
            // @Valid 위반: studentId 공백, email 형식 아님
            return """
        {
          "studentId":" ",
          "email":"bad-email",
          "name":"",
          "phoneNumber":"",
          "department":"",
          "answerList":[]
        }
        """;
        }

        @Test
        @DisplayName("requiresConfirmation=true → 202 ACCEPTED")
        void
        returnsAccepted_whenRequiresConfirmationTrue() throws Exception {
            long clubId = 1L;

            // overwrite 파라미터 기본값(false)
            when(applicationService.submitApplication(
                    eq(clubId),
                    any(ApplicationApplyRequestDto.class),
                    eq(false))
            ).thenReturn(new ApplicationApplyResponseDto(
                    "20231234", now(), true
            ));

            mockMvc.perform(post("/api/clubs/{clubId}/apply-submit", clubId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validPayloadJson()))
                    .andExpect(status().isAccepted());

            verify(applicationService).submitApplication(
                    eq(clubId),
                    any(ApplicationApplyRequestDto.class),
                    eq(false)
            );
        }

        @Test
        @DisplayName("requiresConfirmation=false → 201 CREATED (overwrite=true)")
        void returnsCreated_whenRequiresConfirmationFalse() throws Exception {
            long clubId = 1L;

            when(applicationService.submitApplication(
                    eq(clubId),
                    any(ApplicationApplyRequestDto.class),
                    eq(true))
            ).thenReturn(new ApplicationApplyResponseDto(
                    "20231234", now(), false
            ));

            mockMvc.perform(post("/api/clubs/{clubId}/apply-submit?overwrite=true", clubId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validPayloadJson()))
                    .andExpect(status().isCreated());

            verify(applicationService).submitApplication(
                    eq(clubId),
                    any(ApplicationApplyRequestDto.class),
                    eq(true)
            );
        }

        @Test
        @DisplayName("@Valid 위반 → 400 BAD_REQUEST & 서비스 미호출")
        void returnsBadRequest_whenInvalidBody() throws Exception {
            mockMvc.perform(post("/api/clubs/{clubId}/apply-submit", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidPayloadJson()))
                    .andExpect(status().isBadRequest());

            verify(applicationService, never()).submitApplication(
                    anyLong(),
                    any(ApplicationApplyRequestDto.class),
                    anyBoolean()
            );
        }
    }
}
