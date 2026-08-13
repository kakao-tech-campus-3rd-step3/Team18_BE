package com.kakaotech.team18.backend_server.domain.notification.webhook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SolapiWebhookControllerTest {

    private static final String VALID_HASH = "fe1bae27cb7c1fb823f496f286e78f1d2ae87734";

    private final ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new SolapiWebhookController(
                new SolapiWebhookSecretVerifier("test-secret"),
                publisher
        )).build();
    }

    @Test
    void acceptsValidSingleReportAndPublishesForAsyncProcessing() throws Exception {
        mockMvc.perform(post("/api/webhooks/solapi")
                        .header("X-Solapi-Event-Name", "SINGLE-REPORT")
                        .header("X-Solapi-Secret", VALID_HASH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [{
                                  "messageId": "message-1",
                                  "groupId": "group-1",
                                  "type": "SMS",
                                  "statusCode": "4000",
                                  "statusMessage": "수신 완료",
                                  "dateProcessed": "2026-08-13T04:00:00Z"
                                }]
                                """))
                .andExpect(status().isOk());

        verify(publisher).publishEvent(any(SolapiWebhookReceivedEvent.class));
    }

    @Test
    void rejectsInvalidSecretBeforePublishing() throws Exception {
        mockMvc.perform(post("/api/webhooks/solapi")
                        .header("X-Solapi-Event-Name", "SINGLE-REPORT")
                        .header("X-Solapi-Secret", "wrong")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isUnauthorized());

        verify(publisher, never()).publishEvent(any());
    }

    @Test
    void rejectsUnsupportedEventName() throws Exception {
        mockMvc.perform(post("/api/webhooks/solapi")
                        .header("X-Solapi-Event-Name", "GROUP-REPORT")
                        .header("X-Solapi-Secret", VALID_HASH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isBadRequest());
    }
}
