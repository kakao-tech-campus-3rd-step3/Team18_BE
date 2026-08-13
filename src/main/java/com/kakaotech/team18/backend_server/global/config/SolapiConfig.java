package com.kakaotech.team18.backend_server.global.config;

import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiMessageClient;
import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiSdkMessageClient;
import com.kakaotech.team18.backend_server.domain.notification.sender.NotificationSender;
import com.kakaotech.team18.backend_server.domain.notification.sender.SmsNotificationSender;
import com.kakaotech.team18.backend_server.domain.notification.quota.SolapiSendQuota;
import com.kakaotech.team18.backend_server.domain.notification.repository.NotificationDeliveryRepository;
import com.kakaotech.team18.backend_server.domain.notification.service.NotificationDeliveryStateService;
import com.kakaotech.team18.backend_server.domain.notification.service.SolapiStatusSyncScheduler;
import com.kakaotech.team18.backend_server.domain.notification.sms.SmsMessagePolicy;
import com.kakaotech.team18.backend_server.domain.notification.webhook.SolapiWebhookSecretVerifier;
import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.service.DefaultMessageService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

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
            SolapiSendQuota sendQuota
    ) {
        return new SmsNotificationSender(messageClient, messagePolicy, sendQuota);
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
}
