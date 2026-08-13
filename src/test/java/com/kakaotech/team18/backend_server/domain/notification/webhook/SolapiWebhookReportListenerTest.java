package com.kakaotech.team18.backend_server.domain.notification.webhook;

import com.kakaotech.team18.backend_server.domain.notification.service.NotificationDeliveryStateService;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class SolapiWebhookReportListenerTest {

    private final NotificationDeliveryStateService stateService = mock(NotificationDeliveryStateService.class);
    private final Clock clock = Clock.fixed(
            Instant.parse("2026-08-13T04:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );
    private final SolapiWebhookReportListener listener =
            new SolapiWebhookReportListener(stateService, clock);

    @Test
    void appliesReportUsingProviderMessageIdWithoutRecipientData() {
        listener.handle(new SolapiWebhookReceivedEvent(List.of(new SolapiWebhookReport(
                "message-1",
                "group-1",
                "SMS",
                "5000",
                "수신 실패",
                OffsetDateTime.parse("2026-08-13T04:01:00Z"),
                java.util.Map.of()
        ))));

        verify(stateService).applyWebhookReport(
                "message-1",
                "5000",
                java.time.LocalDateTime.of(2026, 8, 13, 13, 1)
        );
    }

    @Test
    void ignoresReportWithoutMessageId() {
        listener.handle(new SolapiWebhookReceivedEvent(List.of(new SolapiWebhookReport(
                null, null, "SMS", "4000", null, null, java.util.Map.of()
        ))));

        verifyNoInteractions(stateService);
    }
}
