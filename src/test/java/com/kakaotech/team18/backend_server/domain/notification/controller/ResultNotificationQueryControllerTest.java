package com.kakaotech.team18.backend_server.domain.notification.controller;

import com.kakaotech.team18.backend_server.domain.application.entity.Stage;
import com.kakaotech.team18.backend_server.domain.notification.dto.ResultNotificationRequestSummariesResponse;
import com.kakaotech.team18.backend_server.domain.notification.dto.ResultNotificationRequestSummary;
import com.kakaotech.team18.backend_server.domain.notification.service.ResultNotificationQueryService;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationRequestStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ResultNotificationQueryControllerTest {

    private final ResultNotificationQueryService queryService = mock(ResultNotificationQueryService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new ResultNotificationQueryController(queryService)
        ).build();
    }

    @Test
    void returnsRequestLevelCountsWithoutRecipientOrMessageBody() throws Exception {
        given(queryService.findRequestSummaries(1L, 20)).willReturn(
                new ResultNotificationRequestSummariesResponse(List.of(
                        new ResultNotificationRequestSummary(
                                10L,
                                "announcement-key",
                                Stage.FINAL,
                                NotificationRequestStatus.COMPLETED,
                                LocalDateTime.of(2026, 8, 13, 13, 0),
                                5L,
                                1L,
                                1L,
                                2L,
                                1L,
                                0L,
                                1L,
                                1L,
                                new java.math.BigDecimal("70")
                        )
                ))
        );

        mockMvc.perform(get("/api/clubs/1/result-notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requests[0].requestId").value(10))
                .andExpect(jsonPath("$.requests[0].total").value(5))
                .andExpect(jsonPath("$.requests[0].pending").value(1))
                .andExpect(jsonPath("$.requests[0].accepted").value(1))
                .andExpect(jsonPath("$.requests[0].sent").value(2))
                .andExpect(jsonPath("$.requests[0].failed").value(1))
                .andExpect(jsonPath("$.requests[0].sms").value(1))
                .andExpect(jsonPath("$.requests[0].lms").value(1))
                .andExpect(jsonPath("$.requests[0].estimatedCost").value(70))
                .andExpect(jsonPath("$.requests[0].recipientAddress").doesNotExist())
                .andExpect(jsonPath("$.requests[0].messageBody").doesNotExist());

        verify(queryService).findRequestSummaries(1L, 20);
    }
}
