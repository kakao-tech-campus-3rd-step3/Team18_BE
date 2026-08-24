package com.kakaotech.team18.backend_server.global.config;

import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiMessageClient;
import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiSdkMessageClient;
import com.kakaotech.team18.backend_server.domain.notification.sender.NotificationSender;
import com.kakaotech.team18.backend_server.domain.notification.sender.SmsNotificationSender;
import com.kakaotech.team18.backend_server.domain.notification.quota.SolapiSendQuota;
import com.kakaotech.team18.backend_server.domain.notification.repository.NotificationDeliveryRepository;
import com.kakaotech.team18.backend_server.domain.notification.service.NotificationDeliveryStateService;
import com.kakaotech.team18.backend_server.domain.notification.service.SolapiStatusSyncScheduler;
import com.kakaotech.team18.backend_server.domain.notification.service.SolapiBalanceMonitor;
import com.kakaotech.team18.backend_server.domain.notification.service.SolapiAccountQuotaMonitor;
import com.kakaotech.team18.backend_server.domain.notification.sms.SmsMessagePolicy;
import com.kakaotech.team18.backend_server.domain.notification.webhook.SolapiWebhookSecretVerifier;
import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiRecipientAllowlist;
import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.service.DefaultMessageService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import java.math.BigDecimal;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SolapiProperties.class)
@ConditionalOnProperty(prefix = "solapi", name = "enabled", havingValue = "true")
public class SolapiConfig {

    @Bean
    public DefaultMessageService solapiMessageService(SolapiProperties properties) {
        properties.validateEnabledConfiguration();
        return SolapiClient.INSTANCE.createInstance(
                properties.getApiKey(),
                properties.getApiSecret()
        );
    }

    @Bean
    public SolapiMessageClient solapiMessageClient(
            DefaultMessageService messageService,
            SolapiProperties properties
    ) {
        return new SolapiSdkMessageClient(messageService, properties.getSenderNumber());
    }

    @Bean
    public SolapiWebhookSecretVerifier solapiWebhookSecretVerifier(SolapiProperties properties) {
        return new SolapiWebhookSecretVerifier(properties.getWebhookSecret());
    }

    @Bean
    public NotificationSender smsNotificationSender(
            SolapiMessageClient messageClient,
            SmsMessagePolicy messagePolicy,
            SolapiSendQuota sendQuota,
            SolapiRecipientAllowlist recipientAllowlist
    ) {
        return new SmsNotificationSender(messageClient, messagePolicy, sendQuota, recipientAllowlist);
    }

    @Bean
    public SolapiRecipientAllowlist solapiRecipientAllowlist(
            @Value("${solapi.recipient-allowlist-enabled:true}") boolean enabled,
            @Value("${solapi.recipient-allowlist:}") String recipients
    ) {
        return new SolapiRecipientAllowlist(enabled, recipients);
    }

    @Bean
    public SolapiStatusSyncScheduler solapiStatusSyncScheduler(
            NotificationDeliveryRepository repository,
            NotificationDeliveryStateService stateService,
            SolapiMessageClient messageClient,
            @Value("${notification.status.batch-size:50}") int batchSize,
            @Value("${notification.status.check-interval-seconds:60}") long checkIntervalSeconds,
            @Value("${notification.status.max-accepted-age-hours:24}") long maxAcceptedAgeHours
    ) {
        return new SolapiStatusSyncScheduler(
                repository,
                stateService,
                messageClient,
                batchSize,
                checkIntervalSeconds,
                maxAcceptedAgeHours
        );
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "notification.solapi-balance",
            name = "enabled",
            havingValue = "true"
    )
    public SolapiBalanceMonitor solapiBalanceMonitor(
            SolapiMessageClient messageClient,
            @Value("${notification.solapi-balance.low-threshold:5000}") BigDecimal lowThreshold
    ) {
        return new SolapiBalanceMonitor(messageClient, lowThreshold);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "notification.solapi-quota-monitor",
            name = "enabled",
            havingValue = "true"
    )
    public SolapiAccountQuotaMonitor solapiAccountQuotaMonitor(
            SolapiMessageClient messageClient,
            @Value("${notification.result.max-solapi-calls-per-day:50}") int dailyLimit
    ) {
        return new SolapiAccountQuotaMonitor(messageClient, dailyLimit);
    }
}
