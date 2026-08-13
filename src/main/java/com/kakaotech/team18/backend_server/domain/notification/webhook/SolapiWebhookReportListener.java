package com.kakaotech.team18.backend_server.domain.notification.webhook;

import com.kakaotech.team18.backend_server.domain.notification.service.NotificationDeliveryStateService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "solapi", name = "enabled", havingValue = "true")
public class SolapiWebhookReportListener {

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    private final NotificationDeliveryStateService stateService;
    private final Clock clock;

    @Autowired
    public SolapiWebhookReportListener(NotificationDeliveryStateService stateService) {
        this(stateService, Clock.system(SERVICE_ZONE));
    }

    SolapiWebhookReportListener(NotificationDeliveryStateService stateService, Clock clock) {
        this.stateService = stateService;
        this.clock = clock;
    }

    @Async
    @EventListener
    public void handle(SolapiWebhookReceivedEvent event) {
        for (SolapiWebhookReport report : event.reports()) {
            if (report.messageId() == null || report.messageId().isBlank()) {
                log.warn("SOLAPI webhook report ignored because messageId is missing");
                continue;
            }
            boolean applied = stateService.applyWebhookReport(
                    report.messageId(),
                    report.statusCode(),
                    processedAt(report)
            );
            if (!applied) {
                log.warn(
                        "SOLAPI webhook report was not applied: messageId={} statusCode={}",
                        report.messageId(),
                        report.statusCode()
                );
            }
        }
    }

    private LocalDateTime processedAt(SolapiWebhookReport report) {
        return report.dateProcessed() == null
                ? LocalDateTime.now(clock)
                : LocalDateTime.ofInstant(report.dateProcessed().toInstant(), SERVICE_ZONE);
    }
}
