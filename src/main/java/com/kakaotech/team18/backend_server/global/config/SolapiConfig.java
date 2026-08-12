package com.kakaotech.team18.backend_server.global.config;

import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiMessageClient;
import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiSdkMessageClient;
import com.kakaotech.team18.backend_server.domain.notification.sender.NotificationSender;
import com.kakaotech.team18.backend_server.domain.notification.sender.SmsNotificationSender;
import com.kakaotech.team18.backend_server.domain.notification.quota.SolapiSendQuota;
import com.kakaotech.team18.backend_server.domain.notification.sms.SmsMessagePolicy;
import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.service.DefaultMessageService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public NotificationSender smsNotificationSender(
            SolapiMessageClient messageClient,
            SmsMessagePolicy messagePolicy,
            SolapiSendQuota sendQuota
    ) {
        return new SmsNotificationSender(messageClient, messagePolicy, sendQuota);
    }
}
