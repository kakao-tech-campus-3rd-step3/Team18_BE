package com.kakaotech.team18.backend_server.domain.club.controller;

import com.kakaotech.team18.backend_server.global.config.SecurityConfig;
import com.kakaotech.team18.backend_server.global.config.TestSecurityConfig;
import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDashBoardResponseDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDetailResponseDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubListResponseDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubListResponseDto.ClubsInfo;
import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.service.ClubService;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ApplicantResponseDto;
import com.kakaotech.team18.backend_server.global.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;

import static com.kakaotech.team18.backend_server.domain.club.entity.Category.LITERATURE;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ClubController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
        }
)
@Import(TestSecurityConfig.class)
class ClubControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClubService clubService;

    @DisplayName("전체 동아리 목록을 조회한다.")
    @Test
    void getAllClubs_test() throws Exception {
        // given
        ClubListResponseDto.ClubsInfo club1 = new ClubListResponseDto.ClubsInfo(
                1L,
                "동아리1",
                Category.STUDY,
                "짧은 소개1",
                "모집중"
        );

        ClubListResponseDto.ClubsInfo club2 = new ClubListResponseDto.ClubsInfo(
                2L,
                "동아리2",
                Category.SPORTS,
                "짧은 소개2",
                "모집 종료"
        );

        ClubListResponseDto mockResponse = new ClubListResponseDto(List.of(club1, club2));

        when(clubService.getAllClubs()).thenReturn(mockResponse);

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/clubs"));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.clubs.size()").value(2))
                .andExpect(jsonPath("$.clubs[0].name").value("동아리1"))
                .andExpect(jsonPath("$.clubs[1].recruitStatus").value("모집 종료"))
                .andDo(print());
    }
    @DisplayName("동아리 상세 페이지를 조회한다.")
    @Test
    void getClubDetail_test() throws Exception {
        //given
        long clubId = 1L;

        ClubDetailResponseDto expected = ClubDetailResponseDto.builder()
                .clubName("카태켐")
                .location("공대7호관 201호")
                .category(LITERATURE)
                .shortIntroduction("카카오 부트캠프")
                .introductionImages(List.of("ex.image","ex.image2","ex.image3"))
                .introductionOverview("개발자로 성장할 수 있는 부트캠프입니다.")
                .introductionActivity("총 3단계로 이루어진 코스")
                .introductionIdeal("열심열심")
                .regularMeetingInfo("매주 화요일 오후 6시")
                .recruitStatus("모집중")
                .presidentName("김춘식")
                .presidentPhoneNumber("010-1234-5678")
                .recruitStart(LocalDateTime.of(2025, 9, 3, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 9, 20, 23, 59))
                .build();

        when(clubService.getClubDetail(clubId)).thenReturn(expected);

        //when //then
        mockMvc.perform(get("/api/clubs/{clubId}", clubId))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("동아리 대쉬보드 페이지를 조회한다.")
    @Test
    void getClubDashboard_test() throws Exception {
        //given
        long clubId = 1L;

        ClubDashBoardResponseDto expected = new ClubDashBoardResponseDto(1, 1, "2025-09-15", "2025-09-20",
                List.of(new ApplicantResponseDto("춘식", "123456", "철학과", "010-1234-5678", "email.com", Status.PENDING)));

        //when
        when(clubService.getClubDashBoard(clubId)).thenReturn(expected);

        //then
        mockMvc.perform(get("/api/clubs/{clubId}/dashboard", clubId))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @DisplayName("동아리 대쉬보드에서 지원서의 상태를 통해 지원자를 필터링 조회한다.")
    @Test
    void getApplicantsByStatus() throws Exception {
        //given
        Long clubId = 1L;
        String status = "미정";
        List<ApplicantResponseDto> expect = List.of(
                new ApplicantResponseDto("김춘식", "111111", "철학과", "010-1234-5678", "123@email.com",
                        Status.PENDING),
                new ApplicantResponseDto("김춘식", "222222", "철학과", "010-1234-5678", "123@email.com",
                        Status.PENDING)
        );

        //when
        when(clubService.getApplicantsByStatus(clubId, Status.PENDING)).thenReturn(expect);

        //then
        mockMvc.perform(get("/api/clubs/{clubId}/dashboard/applicants", clubId)
                        .param("status", status))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("동아리 대쉬보드에서 지원서의 상태를 통해 지원자를 필터링 조회시 Status 값이 비어 있으면 모든 지원자를 조회한다.")
    @Test
    void getApplicantsByStatusWithEmptyStatus() throws Exception {
        //given
        Long clubId = 1L;
        String status = null;
        List<ApplicantResponseDto> expect = List.of(
                new ApplicantResponseDto("김춘식", "111111", "철학과", "010-1234-5678", "123@email.com",
                        Status.PENDING),
                new ApplicantResponseDto("김춘식", "222222", "철학과", "010-1234-5678", "123@email.com",
                        Status.APPROVED)
        );

        //when
        when(clubService.getApplicantsByStatus(clubId, null)).thenReturn(expect);

        //then
        mockMvc.perform(get("/api/clubs/{clubId}/dashboard/applicants", clubId)
                        .param("status", status))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("동아리 대쉬보드에서 지원서의 상태를 통해 지원자를 필터링 조회시 Status에 등록되지 않은 쿼리파라미터를 주면 404NotFoud에러가 발생하낟.")
    @Test
    void getApplicantsByWrongStatus() throws Exception {
        // given
        Long clubId = 1L;
        String wrongStatus = "엉망값";

        // when + then
        mockMvc.perform(get("/api/clubs/{clubId}/dashboard/applicants", clubId)
                        .param("status", wrongStatus))
                .andDo(print())
                .andExpect(status().isNotFound());

        verifyNoInteractions(clubService);
    }
}
