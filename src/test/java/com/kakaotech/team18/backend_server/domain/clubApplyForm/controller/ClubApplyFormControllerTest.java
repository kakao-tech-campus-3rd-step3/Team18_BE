package com.kakaotech.team18.backend_server.domain.clubApplyForm.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kakaotech.team18.backend_server.domain.formQuestion.dto.FormQuestionRequestDto;
import com.kakaotech.team18.backend_server.domain.formQuestion.dto.FormQuestionResponseDto;
import com.kakaotech.team18.backend_server.domain.formQuestion.dto.FormQuestionUpdateDto;
import com.kakaotech.team18.backend_server.domain.formQuestion.entity.FieldType;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.dto.ClubApplyFormRequestDto;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.dto.ClubApplyFormResponseDto;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.dto.ClubApplyFormUpdateDto;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.service.ClubApplyFormService;
import com.kakaotech.team18.backend_server.global.config.SecurityConfig;
import com.kakaotech.team18.backend_server.global.config.TestSecurityConfig;
import com.kakaotech.team18.backend_server.global.security.JwtAuthenticationFilter;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ClubApplyFormNotFoundException;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ClubNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = ClubApplyFormController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
        }
)
@Import(TestSecurityConfig.class)
class ClubApplyFormControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    private ClubApplyFormService clubApplyFormService;

    @DisplayName("동아리 지원서 양식 조회 테스트 - 성공")
    @Test
    void getClubApplyFormByClubId_success() throws Exception {
        // given
        Long clubId = 1L;
        FormQuestionResponseDto question1 = new FormQuestionResponseDto(1L,1L, FieldType.TEXT, "이름", true, null, null);
        FormQuestionResponseDto question2 = new FormQuestionResponseDto(2L, 2L, FieldType.RADIO, "성별", true, List.of("남", "여"), null);
        ClubApplyFormResponseDto mockResponse = ClubApplyFormResponseDto.of(
                "테스트 동아리 지원서",
                "테스트 동아리 지원서 설명입니다.",
                List.of(question1, question2)
        );

        when(clubApplyFormService.getQuestionForm(clubId)).thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/clubs/{clubId}/apply", clubId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("테스트 동아리 지원서"))
                .andExpect(jsonPath("$.description").value("테스트 동아리 지원서 설명입니다."))
                .andExpect(jsonPath("$.questions[0].question").value("이름"))
                .andExpect(jsonPath("$.questions[1].question").value("성별"))
                .andExpect(jsonPath("$.questions[1].optionList[0]").value("남"));

        verify(clubApplyFormService, times(1)).getQuestionForm(clubId);
    }
    @DisplayName("대시보드 api 동아리 지원서 양식 조회 테스트 - 성공")
    @Test
    void getClubApplyFormByClubIdInDashboard_success() throws Exception {
        // given
        Long clubId = 1L;
        FormQuestionResponseDto question1 = new FormQuestionResponseDto(1L, 1L, FieldType.TEXT, "이름", true, null, null);
        FormQuestionResponseDto question2 = new FormQuestionResponseDto(2L, 2L, FieldType.RADIO, "성별", true, List.of("남", "여"), null);
        ClubApplyFormResponseDto mockResponse = ClubApplyFormResponseDto.of(
                "테스트 동아리 지원서",
                "테스트 동아리 지원서 설명입니다.",
                List.of(question1, question2)
        );

        when(clubApplyFormService.getQuestionForm(clubId)).thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/clubs/{clubId}/dashboard/apply-form", clubId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("테스트 동아리 지원서"))
                .andExpect(jsonPath("$.description").value("테스트 동아리 지원서 설명입니다."))
                .andExpect(jsonPath("$.questions[0].question").value("이름"))
                .andExpect(jsonPath("$.questions[1].question").value("성별"))
                .andExpect(jsonPath("$.questions[1].optionList[0]").value("남"));

        verify(clubApplyFormService, times(1)).getQuestionForm(clubId);
    }

    @DisplayName("동아리 지원서 양식 조회 테스트 - 지원서 양식 없음")
    @Test
    void getClubApplyFormByClubId_notFound() throws Exception {
        // given
        Long clubId = 999L;
        when(clubApplyFormService.getQuestionForm(clubId))
                .thenThrow(new ClubApplyFormNotFoundException("clubId = " + clubId));

        // when & then
        mockMvc.perform(get("/api/clubs/{clubId}/apply", clubId))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(clubApplyFormService, times(1)).getQuestionForm(clubId);
    }
    @DisplayName("대시보드 api 동아리 지원서 양식 조회 테스트 - 지원서 양식 없음")
    @Test
    void getClubApplyFormByClubIdInDashboard_notFound() throws Exception {
        // given
        Long clubId = 999L;
        when(clubApplyFormService.getQuestionForm(clubId))
                .thenThrow(new ClubApplyFormNotFoundException("clubId = " + clubId));

        // when & then
        mockMvc.perform(get("/api/clubs/{clubId}/dashboard/apply-form", clubId))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(clubApplyFormService, times(1)).getQuestionForm(clubId);
    }

    @DisplayName("동아리 지원서 저장 API 호출 - 성공")
    @Test
    void createClubApplyForm() throws Exception {
        //given
        Long clubId = 1L;
        ClubApplyFormRequestDto clubApplyFormRequestDto = new ClubApplyFormRequestDto(
                "테스트 지원서",
                "테스트 설명",
                LocalDateTime.of(2025, 10, 1, 0, 0),
                LocalDateTime.of(2025, 10, 31, 23, 59),
                List.of(new FormQuestionRequestDto("질문 1", FieldType.TEXT, true, 1L, null, null))
                );

        doNothing().when(clubApplyFormService).createClubApplyForm(clubId, clubApplyFormRequestDto);

        //when & then
        mockMvc.perform(post("/api/clubs/{clubId}/dashboard/apply-form", clubId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clubApplyFormRequestDto))
                )
                .andDo(print())
                .andExpect(status().isCreated());

        verify(clubApplyFormService, times(1)).createClubApplyForm(clubId, clubApplyFormRequestDto);
    }


    @DisplayName("동아리 지원서 저장 API 호출 - 실패(해당 동아리가 없음)")
    @Test
    void createClubApplyForm_wrongClubId() throws Exception {
        //given
        Long clubId = 1L;
        ClubApplyFormRequestDto clubApplyFormRequestDto = new ClubApplyFormRequestDto(
                "테스트 지원서",
                "테스트 설명",
                LocalDateTime.of(2025, 10, 1, 0, 0),
                LocalDateTime.of(2025, 10, 31, 23, 59),
                List.of(new FormQuestionRequestDto("질문 1", FieldType.TEXT, true, 1L, null, null))
        );

        doThrow(new ClubNotFoundException("clubId")).when(clubApplyFormService).createClubApplyForm(clubId, clubApplyFormRequestDto);

        //when & then
        mockMvc.perform(post("/api/clubs/{clubId}/dashboard/apply-form", clubId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clubApplyFormRequestDto))
                )
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(clubApplyFormService, times(1)).createClubApplyForm(clubId, clubApplyFormRequestDto);
    }

    @DisplayName("동아리 지원서 저장 API 호출 - 실패(유효하지 않은 입력)")
    @Test
    void createClubApplyForm_invalidInput() throws Exception {
        //given
        Long clubId = 1L;
        // question 필드가 blank인 경우
        ClubApplyFormRequestDto clubApplyFormRequestDto = new ClubApplyFormRequestDto("테스트 지원서", "테스트 설명",
                LocalDateTime.of(2025, 10, 1, 0, 0),
                LocalDateTime.of(2025, 10, 31, 23, 59),
                List.of(new FormQuestionRequestDto("", FieldType.TEXT, true, 1L, null, null)));

        //when & then
        mockMvc.perform(post("/api/clubs/{clubId}/dashboard/apply-form", clubId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clubApplyFormRequestDto))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력 값이 올바르지 않습니다."));

        verifyNoInteractions(clubApplyFormService);
    }

    @DisplayName("동아리 지원서 저장 API 호출 - 실패(TIME_SLOT 질문인데 timeSlotOptions가 null이면 400 응답)")
    @Test
    void timeSlotQuestionWithoutOptions_shouldFailValidation() throws Exception {
        Long clubId = 1L;
        ClubApplyFormRequestDto invalidRequestDto = new ClubApplyFormRequestDto(
                "테스트 지원서",
                "설명",
                LocalDateTime.of(2025, 10, 1, 0, 0),
                LocalDateTime.of(2025, 10, 31, 23, 59),
                List.of(new FormQuestionRequestDto("면접 가능한 시간대를 선택해 주세요.", FieldType.TIME_SLOT, true, 1L, null, null))
        );

        mockMvc.perform(post("/api/clubs/{clubId}/dashboard/apply-form", clubId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequestDto))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @DisplayName("동아리 지원서 수정 API 호출 - 성공")
    @Test
    void updateClubApplyForm() throws Exception {
        //given
        Long clubId = 1L;
        ClubApplyFormUpdateDto clubApplyFormUpdateDto = new ClubApplyFormUpdateDto(
                "테스트 지원서",
                "테스트 설명",
                LocalDateTime.of(2025, 10, 1, 0, 0),
                LocalDateTime.of(2025, 10, 31, 23, 59),
                List.of(new FormQuestionUpdateDto(1L, "질문 1", FieldType.TEXT, true, 1L, null, null)
                ));

        doNothing().when(clubApplyFormService).updateClubApplyForm(clubId, clubApplyFormUpdateDto);

        //when & then
        mockMvc.perform(patch("/api/clubs/{clubId}/dashboard/apply-form", clubId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clubApplyFormUpdateDto))
                )
                .andDo(print())
                .andExpect(status().isAccepted());

        verify(clubApplyFormService, times(1)).updateClubApplyForm(clubId, clubApplyFormUpdateDto);
    }

    @DisplayName("동아리 지원서 수정 API 호출 - 실패(해당 동아리가 없음)")
    @Test
    void updateClubApplyForm_wrongClubId() throws Exception {
        //given
        Long clubId = 1L;
        ClubApplyFormUpdateDto clubApplyFormUpdateDto = new ClubApplyFormUpdateDto(
                "테스트 지원서",
                "테스트 설명",
                LocalDateTime.of(2025, 10, 1, 0, 0),
                LocalDateTime.of(2025, 10, 31, 23, 59),
                List.of(new FormQuestionUpdateDto(1L, "질문 1", FieldType.TEXT, true, 1L, null, null)
                ));

        doThrow(new ClubNotFoundException("clubId")).when(clubApplyFormService).updateClubApplyForm(clubId, clubApplyFormUpdateDto);

        //when & then
        mockMvc.perform(patch("/api/clubs/{clubId}/dashboard/apply-form", clubId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clubApplyFormUpdateDto))
                )
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(clubApplyFormService, times(1)).updateClubApplyForm(clubId, clubApplyFormUpdateDto);
    }

    @DisplayName("동아리 지원서 수정 API 호출 - 실패(유효하지 않은 입력)")
    @Test
    void updateClubApplyForm_invalidInput() throws Exception {
        //given
        Long clubId = 1L;
        // question 필드가 blank인 경우
        ClubApplyFormUpdateDto clubApplyFormUpdateDto = new ClubApplyFormUpdateDto(
                "테스트 지원서",
                "테스트 설명",
                LocalDateTime.of(2025, 10, 1, 0, 0),
                LocalDateTime.of(2025, 10, 31, 23, 59),
                List.of(new FormQuestionUpdateDto(1L, "", FieldType.TEXT, true, 1L, null, null)));

        //when & then
        mockMvc.perform(patch("/api/clubs/{clubId}/dashboard/apply-form", clubId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clubApplyFormUpdateDto))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력 값이 올바르지 않습니다."));

        verifyNoInteractions(clubApplyFormService);
    }

    @DisplayName("동아리 지원서 수정 API 호출 - 실패(TIME_SLOT 질문인데 timeSlotOptions가 null이면 400 응답)")
    @Test
    void updateClubApplyForm_timeSlotQuestionWithoutOptions_shouldFailValidation() throws Exception {
        Long clubId = 1L;
        ClubApplyFormUpdateDto invalidRequestDto = new ClubApplyFormUpdateDto(
                "테스트 지원서",
                "설명",
                LocalDateTime.of(2025, 10, 1, 0, 0),
                LocalDateTime.of(2025, 10, 31, 23, 59),
                List.of(new FormQuestionUpdateDto(1L, "면접 가능한 시간대를 선택해 주세요.", FieldType.TIME_SLOT, true, 1L, null, null))
        );

        mockMvc.perform(patch("/api/clubs/{clubId}/dashboard/apply-form", clubId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequestDto))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }



}