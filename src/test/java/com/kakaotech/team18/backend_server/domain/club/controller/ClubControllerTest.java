package com.kakaotech.team18.backend_server.domain.club.controller;

import static com.kakaotech.team18.backend_server.domain.club.entity.Category.LITERATURE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kakaotech.team18.backend_server.domain.application.entity.Stage;
import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDashBoardResponseDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDashboardApplicantResponseDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDetailRequestDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubDetailResponseDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubListResponseDto;
import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.service.ClubService;
import com.kakaotech.team18.backend_server.domain.clubMember.dto.ApplicantResponseDto;
import com.kakaotech.team18.backend_server.global.config.SecurityConfig;
import com.kakaotech.team18.backend_server.global.config.TestSecurityConfig;
import com.kakaotech.team18.backend_server.global.dto.SuccessResponseDto;
import com.kakaotech.team18.backend_server.global.security.JwtAuthenticationFilter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

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

    @DisplayName("전체 동아리 목록을 필터링 조회한다.")
    @Test
    void getAllClubs_byCategory_test() throws Exception {
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

        String category = "STUDY";

        ClubListResponseDto mockResponse = new ClubListResponseDto(List.of(club1));

        when(clubService.getClubByCategory(category)).thenReturn(mockResponse);

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/clubs").param("category", category));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.clubs.size()").value(1))
                .andExpect(jsonPath("$.clubs[0].name").value("동아리1"))
                .andExpect(jsonPath("$.clubs[0].recruitStatus").value("모집중"))
                .andDo(print());
    }

    @DisplayName("동아리 상세 페이지를 조회한다.")
    @Test
    void getClubDetail_test() throws Exception {
        //given
        long clubId = 1L;

        ClubDetailResponseDto expected = ClubDetailResponseDto.builder()
                .clubId(clubId)
                .clubName("카태켐")
                .location("공대7호관 201호")
                .category(LITERATURE)
                .shortIntroduction("카카오 부트캠프")
                .introductionImages(List.of(new ClubDetailResponseDto.ClubImageResponseDto(1L, "ex.image"),
                        new ClubDetailResponseDto.ClubImageResponseDto(2L, "ex.image2"),
                        new ClubDetailResponseDto.ClubImageResponseDto(3L, "ex.image3")))
                .introductionOverview("개발자로 성장할 수 있는 부트캠프입니다.")
                .introductionActivity("총 3단계로 이루어진 코스")
                .introductionIdeal("열심열심")
                .regularMeetingInfo("매주 화요일 오후 6시")
                .recruitStatus("모집중")
                .presidentName("김춘식")
                .presidentPhoneNumber("010-1234-5678")
                .recruitStart(LocalDateTime.of(2025, 9, 3, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 9, 20, 23, 59))
                .applicationNotice("주의사항")
                .build();

        when(clubService.getClubDetail(clubId)).thenReturn(expected);

        //when //then
        mockMvc.perform(get("/api/clubs/{clubId}", clubId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clubId").value(clubId));
    }

    @DisplayName("동아리 대쉬보드 페이지를 조회한다.")
    @Test
    void getClubDashboard_test() throws Exception {
        //given
        long clubId = 1L;

        ClubDashBoardResponseDto expected = new ClubDashBoardResponseDto(1L,
                1,
                1,
                LocalDate.of(2025, 9, 15),
                LocalDate.of(2025, 9, 20)
                );

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
        String stage = String.valueOf(Stage.INTERVIEW);
        ClubDashboardApplicantResponseDto expect = new ClubDashboardApplicantResponseDto(
                List.of(
                new ApplicantResponseDto("김춘식", "111111", "철학과", "010-1234-5678", "123@email.com",
                        Status.PENDING, 1L),
                new ApplicantResponseDto("김춘식", "222222", "철학과", "010-1234-5678", "123@email.com",
                        Status.PENDING, 2L)),
                "message");

        //when
        when(clubService.getApplicantsByStatusAndStage(clubId, Status.PENDING, Stage.INTERVIEW)).thenReturn(expect);

        //then
        mockMvc.perform(get("/api/clubs/{clubId}/dashboard/applicants", clubId)
                        .param("status", status)
                .param("stage", stage))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("동아리 대쉬보드에서 지원서의 상태를 통해 지원자를 필터링 조회시 Status 값이 비어 있으면 모든 지원자를 조회한다.")
    @Test
    void getApplicantsByStatusWithEmptyStatus() throws Exception {
        //given
        Long clubId = 1L;
        String status = null;
        String stage = String.valueOf(Stage.INTERVIEW);
        ClubDashboardApplicantResponseDto expect = new ClubDashboardApplicantResponseDto(
                List.of(
                new ApplicantResponseDto("김춘식", "111111", "철학과", "010-1234-5678", "123@email.com",
                        Status.PENDING, 1L),
                new ApplicantResponseDto("김춘식", "222222", "철학과", "010-1234-5678", "123@email.com",
                        Status.APPROVED, 2L)),
                "message"
        );

        //when
        when(clubService.getApplicantsByStatusAndStage(clubId, null, Stage.INTERVIEW)).thenReturn(expect);

        //then
        mockMvc.perform(get("/api/clubs/{clubId}/dashboard/applicants", clubId)
                        .param("status", status)
                .param("stage", stage))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("동아리 대쉬보드에서 지원서의 상태를 통해 지원자를 필터링 조회시 Status에 등록되지 않은 쿼리파라미터를 주면 404NotFoud에러가 발생한다.")
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

    @DisplayName("동아리 상세페이지 내용을 변경한다.")
    @Test
    void updateClubDetail() throws Exception {
        //given
        Long clubId = 1L;
        ClubDetailRequestDto requestDto = ClubDetailRequestDto.builder()
                .clubName("새로운동아리")
                .clubId(clubId)
                .category(Category.SPORTS)
                .location("인문대 2호관")
                .shortIntroduction("new short")
                .introductionOverview("new overview")
                .introductionActivity("new activity")
                .introductionIdeal("new ideal")
                .applicationNotice("주의사항")
                .regularMeetingInfo("매주 수 18:00")
                .build();

        //when
        when(clubService.updateClubDetail(clubId, requestDto)).thenReturn(new SuccessResponseDto(true));

        //then
        mockMvc.perform(post("/api/clubs/{clubId}", clubId)
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @DisplayName("동아리 상세페이지 내용 변경시 유효성 검사 실패")
    @Test
    void updateClubDetail_validationFail() throws Exception {
        //given
        Long clubId = 1L;
        ClubDetailRequestDto requestDto = ClubDetailRequestDto.builder()
                .clubId(clubId)
                .clubName("") // Blank
                .category(Category.SPORTS)
                .location("인문대 2호관")
                .shortIntroduction("new short")
                .introductionOverview("new overview")
                .introductionActivity("new activity")
                .introductionIdeal("new ideal")
                .applicationNotice("주의사항")
                .regularMeetingInfo("매주 수 18:00")
                .build();

        //when //then
        mockMvc.perform(post("/api/clubs/{clubId}", clubId)
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_code").value("INVALID_INPUT_VALUE"));

        verifyNoInteractions(clubService);
    }

    @DisplayName("동아리 상세 페이지 수정에서 이미지를 변경할 수 있다.")
    @Test
    void updateClubImage() throws Exception {
        //given
        Long clubId = 1L;

        MockMultipartFile keepImageIdsPart = new MockMultipartFile(
                "keepImageIds",
                "",
                "application/json",
                "[1,2]".getBytes()
        );

        MockMultipartFile newImage = new MockMultipartFile(
                "newImages",
                "newImage.jpg",
                "image/jpeg",
                "image-content".getBytes()
        );

        //when
        mockMvc.perform(
                        multipart("/api/clubs/{clubId}/images", clubId)
                                .file(keepImageIdsPart)
                                .file(newImage)
                                .with(request -> { request.setMethod("PATCH"); return request; })
                )
                .andDo(print())
                .andExpect(status().isOk());

        //then
        verify(clubService).uploadClubImages(eq(clubId), any(List.class), any(List.class));
    }

    @DisplayName("동아리 상세 페이지 수정에서 이미지를 변경할 때 keepImageIds가 유효하지 않으면 400 에러를 반환한다.")
    @Test
    void updateClubImage_invalidKeepImageIds() throws Exception {
        //given
        Long clubId = 1L;

        MockMultipartFile keepImageIdsPart = new MockMultipartFile(
                "keepImageIds",
                "",
                "application/json",
                "invalid_json".getBytes()
        );

        MockMultipartFile newImage = new MockMultipartFile(
                "newImages",
                "newImage.jpg",
                "image/jpeg",
                "image-content".getBytes()
        );

        //when
        mockMvc.perform(
                        multipart("/api/clubs/{clubId}/images", clubId)
                                .file(keepImageIdsPart)
                                .file(newImage)
                                .with(request -> { request.setMethod("PATCH"); return request; })
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
        //then
        verifyNoInteractions(clubService);
    }

    @DisplayName("동아리 상세 페이지 수정에서 이미지를 변경할 때 keepImageIds가 잘못된 형식으로 오면 400 에러를 반환한다.")
    @Test
    void updateClubImage_malformedKeepImageIds() throws Exception {
        //given
        Long clubId = 1L;

        MockMultipartFile keepImageIdsPart = new MockMultipartFile(
                "keepImageIds",
                "",
                "application/json",
                "not_a_json_array".getBytes()
        );

        MockMultipartFile newImage = new MockMultipartFile(
                "newImages",
                "newImage.jpg",
                "image/jpeg",
                "image-content".getBytes()
        );

        //when
        mockMvc.perform(
                        multipart("/api/clubs/{clubId}/images", clubId)
                                .file(keepImageIdsPart)
                                .file(newImage)
                                .with(request -> { request.setMethod("PATCH"); return request; })
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
        //then
        verifyNoInteractions(clubService);
    }


    @DisplayName("동아리 상세 페이지 수정에서 이미지를 변경할 때 이미지가 없으면 400 에러를 반환한다.")
    @Test
    void updateClubImage_noImage() throws Exception {
        //given
        Long clubId = 1L;

        //when
        mockMvc.perform(
                        multipart("/api/clubs/{clubId}/images", clubId)
                                .with(request -> { request.setMethod("PATCH"); return request; })
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
        //then
        verifyNoInteractions(clubService);
    }
}
