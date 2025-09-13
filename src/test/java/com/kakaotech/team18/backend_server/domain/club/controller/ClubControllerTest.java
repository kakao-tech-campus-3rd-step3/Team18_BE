package com.kakaotech.team18.backend_server.domain.club.controller;

import static com.kakaotech.team18.backend_server.domain.club.entity.Category.LITERATURE;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kakaotech.team18.backend_server.domain.club.dto.ClubDetailResponseDto;
import com.kakaotech.team18.backend_server.domain.club.dto.ClubListResponseDto;
import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.service.ClubService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(ClubController.class)
class ClubControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClubService clubService;

    @DisplayName("전체 동아리 목록을 조회한다.")
    @Test
    void getAllClubs_test() throws Exception {
        // given
        ClubListResponseDto club1 = new ClubListResponseDto(
                1L,
                "동아리1",
                Category.STUDY,
                "짧은 소개1",
                "모집중");
        ClubListResponseDto club2 = new ClubListResponseDto(
                2L,
                "동아리2",
                Category.SPORTS,
                "짧은 소개2",
                "모집 종료");
        List<ClubListResponseDto> mockClubList = List.of(club1, club2);

        when(clubService.getAllClubs()).thenReturn(mockClubList);

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/clubs"));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].name").value("동아리1"))
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
}
