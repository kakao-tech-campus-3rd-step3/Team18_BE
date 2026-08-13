package com.kakaotech.team18.backend_server.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiMessageClient;
import com.kakaotech.team18.backend_server.domain.notification.sender.NotificationSender;
import com.kakaotech.team18.backend_server.domain.notification.quota.SolapiSendQuota;
import com.kakaotech.team18.backend_server.domain.notification.repository.NotificationDeliveryRepository;
import com.kakaotech.team18.backend_server.domain.notification.service.NotificationDeliveryStateService;
import com.kakaotech.team18.backend_server.domain.notification.sms.SmsMessagePolicy;
import com.solapi.sdk.message.service.DefaultMessageService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class SolapiConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(SolapiConfig.class)
            .withBean(SmsMessagePolicy.class, SmsMessagePolicy::new)
            .withBean(SolapiSendQuota.class, () -> message -> { })
            .withBean(NotificationDeliveryRepository.class,
                    () -> org.mockito.Mockito.mock(NotificationDeliveryRepository.class))
            .withBean(NotificationDeliveryStateService.class,
                    () -> org.mockito.Mockito.mock(NotificationDeliveryStateService.class));

    @Test
    void doesNotCreateSolapiBeansWhenDisabled() {
        contextRunner
                .withPropertyValues("solapi.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(DefaultMessageService.class);
                    assertThat(context).doesNotHaveBean(SolapiMessageClient.class);
                    assertThat(context).doesNotHaveBean(NotificationSender.class);
                });
    }

    @Test
    void createsSolapiBeansWhenAllCredentialsExist() {
        contextRunner
                .withPropertyValues(
                        "solapi.enabled=true",
                        "solapi.api-key=test-api-key",
                        "solapi.api-secret=test-api-secret",
                        "solapi.sender-number=01012345678"
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(DefaultMessageService.class);
                    assertThat(context).hasSingleBean(SolapiMessageClient.class);
                    assertThat(context).hasSingleBean(NotificationSender.class);
                });
    }

    @Test
    void failsFastWhenEnabledCredentialIsMissing() {
        contextRunner
                .withPropertyValues(
                        "solapi.enabled=true",
                        "solapi.api-key=test-api-key",
                        "solapi.api-secret=",
                        "solapi.sender-number=01012345678"
                )
                .run(context -> assertThat(context.getStartupFailure())
                        .hasRootCauseInstanceOf(IllegalStateException.class)
                        .rootCause()
                        .hasMessageContaining("SOLAPI_API_SECRET"));
    }

    @Test
    void rejectsFormattedSenderNumber() {
        contextRunner
                .withPropertyValues(
                        "solapi.enabled=true",
                        "solapi.api-key=test-api-key",
                        "solapi.api-secret=test-api-secret",
                        "solapi.sender-number=010-1234-5678"
                )
                .run(context -> assertThat(context.getStartupFailure())
                        .hasRootCauseInstanceOf(IllegalStateException.class)
                        .rootCause()
                        .hasMessageContaining("하이픈 없이"));
    }
}
