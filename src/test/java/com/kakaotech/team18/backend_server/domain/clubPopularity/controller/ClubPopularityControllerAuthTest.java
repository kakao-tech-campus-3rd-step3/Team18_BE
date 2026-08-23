package com.kakaotech.team18.backend_server.domain.clubPopularity.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kakaotech.team18.backend_server.domain.clubPopularity.service.ClubPopularityRecordingService;
import com.kakaotech.team18.backend_server.domain.clubPopularity.service.ClubPopularityQueryService;
import com.kakaotech.team18.backend_server.domain.club.service.ClubService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class ClubPopularityControllerAuthTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClubPopularityRecordingService recordingService;

    @MockBean
    private ClubPopularityQueryService queryService;

    @MockBean
    private ClubService clubService;

    @Test
    @DisplayName("views 기록 API는 인증 헤더 없이 공개된다")
    void viewsIsPublic() throws Exception {
        mockMvc.perform(post("/api/clubs/{clubId}/views", 7L))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("heartbeat 기록 API는 인증 헤더 없이 공개된다")
    void heartbeatIsPublic() throws Exception {
        mockMvc.perform(post("/api/clubs/{clubId}/heartbeat", 7L))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("popular 조회 API는 인증 헤더 없이 공개된다")
    void popularIsPublic() throws Exception {
        org.mockito.Mockito.when(queryService.getPopularClubs()).thenReturn(
                com.kakaotech.team18.backend_server.domain.clubPopularity.dto.ClubPopularityResponse.empty());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/clubs/popular"))
                .andExpect(status().isOk());
    }
}
