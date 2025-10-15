package com.kakaotech.team18.backend_server.domain.clubReview;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kakaotech.team18.backend_server.domain.clubReview.dto.ClubReviewRequestDto;
import com.kakaotech.team18.backend_server.domain.clubReview.service.ClubReviewService;
import com.kakaotech.team18.backend_server.global.config.SecurityConfig;
import com.kakaotech.team18.backend_server.global.dto.SuccessResponseDto;
import com.kakaotech.team18.backend_server.global.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = ClubReviewController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
        }
)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ClubReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    public ClubReviewService clubReviewService;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("사용자가 동아리 리뷰를 등록한다.")
    @Test
    void createClubReview_success() throws Exception {
        //given
        Long clubId = 1L;
        String content = "최고의 개발 동아리입니다.";
        String studentId = "20221234";
        ClubReviewRequestDto requestDto = new ClubReviewRequestDto(content, studentId);
        SuccessResponseDto successResponseDto = new SuccessResponseDto(true);
        given(clubReviewService.createClubReview(clubId, requestDto)).willReturn(successResponseDto);

        //when
        mockMvc.perform(post("/api/clubs/{clubId}/reviews", clubId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(print());

        then(clubReviewService).should(times(1)).createClubReview(clubId, requestDto);
    }

    @DisplayName("사용자가 동아리 리뷰를 등록할 때, studentId가 누락되면 400 에러를 반환한다.")
    @Test
    void createClubReview_missingStudentId_returnsBadRequest() throws Exception {
        // given
        Long clubId = 1L;
        String content = "최고의 개발 동아리입니다.";
        String studentId = ""; // 누락된 studentId
        ClubReviewRequestDto requestDto = new ClubReviewRequestDto(content, studentId);

        // when & then
        mockMvc.perform(post("/api/clubs/{clubId}/reviews", clubId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        then(clubReviewService).should(times(0)).createClubReview(clubId, requestDto);
    }

    @DisplayName("사용자가 동아리 리뷰를 등록할 때, content가 누락되면 400 에러를 반환한다.")
    @Test
    void createClubReview_missingContent_returnsBadRequest() throws Exception {
        // given
        Long clubId = 1L;
        String content = ""; // 누락된 content
        String studentId = "20221234";
        ClubReviewRequestDto requestDto = new ClubReviewRequestDto(content, studentId);

        // when & then
        mockMvc.perform(post("/api/clubs/{clubId}/reviews", clubId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        then(clubReviewService).should(times(0)).createClubReview(clubId, requestDto);
    }

}