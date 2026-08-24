package com.kakaotech.team18.backend_server.domain.notification.webhook;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/webhooks/solapi")
@ConditionalOnProperty(prefix = "solapi", name = "enabled", havingValue = "true")
public class SolapiWebhookController {

    private static final String SINGLE_REPORT = "SINGLE-REPORT";

    private final SolapiWebhookSecretVerifier secretVerifier;
    private final ApplicationEventPublisher eventPublisher;

    @PostMapping
    public ResponseEntity<Void> receive(
            @RequestHeader(value = "X-Solapi-Event-Name", required = false) String eventName,
            @RequestHeader(value = "X-Solapi-Secret", required = false) String secretHash,
            @RequestBody List<SolapiWebhookReport> reports
    ) {
        if (!secretVerifier.matches(secretHash)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!SINGLE_REPORT.equals(eventName)) {
            return ResponseEntity.badRequest().build();
        }
        if (!reports.isEmpty()) {
            eventPublisher.publishEvent(new SolapiWebhookReceivedEvent(reports));
        }
        return ResponseEntity.ok().build();
    }
}
