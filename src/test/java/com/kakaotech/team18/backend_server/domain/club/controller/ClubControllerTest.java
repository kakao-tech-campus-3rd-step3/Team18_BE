package com.kakaotech.team18.backend_server.domain.club.controller;

import static com.kakaotech.team18.backend_server.domain.club.entity.Category.LITERATURE;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.kakaotech.team18.backend_server.domain.club.dto.ClubDetailResponseDto;
import com.kakaotech.team18.backend_server.domain.club.service.ClubService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(ClubController.class)
class ClubControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClubService clubService;

    @DisplayName("동아리 상세 페이지를 조회한다.")
    @Test
    void test() throws Exception {
        //given
        long clubId = 1;

        ClubDetailResponseDto expected = ClubDetailResponseDto.builder()
                .clubName("카태켐")
                .location("공대7호관 201호")
                .category(LITERATURE)
                .shortIntroduction("카카오 부트캠프")
                .introductionImage("ex.image")
                .introductionIntroduce("개발자로 성장할 수 있는 부트캠프입니다.")
                .introductionActivity("총 3단계로 이루어진 코스")
                .introductionWannabe("열심열심")
                .regularMeetingInfo("매주 화요일 오후 6시")
                .recruitStatus("모집중")
                .presidentName("김춘식")
                .presidentPhoneNumber("010-1234-5678")
                .recruitStart(LocalDateTime.of(2025,9,3,0,0))
                .recruitEnd(LocalDateTime.of(2025,9,20,23,59))
                .build();

        when(clubService.getClubDetail(clubId)).thenReturn(expected);

        //when //then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/clubs/{clubId}", 1))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk());

    }


}