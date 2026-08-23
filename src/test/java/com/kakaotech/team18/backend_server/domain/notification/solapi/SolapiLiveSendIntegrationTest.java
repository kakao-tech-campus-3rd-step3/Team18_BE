package com.kakaotech.team18.backend_server.domain.notification.solapi;

import static org.assertj.core.api.Assertions.assertThat;

import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.service.DefaultMessageService;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.core.io.ClassPathResource;

@Disabled("실제 과금되는 SMS를 발송합니다. 수동 검증할 때만 일시적으로 활성화하세요.")
class SolapiLiveSendIntegrationTest {

    private static final String SEND_CONFIRMATION = "SEND_ONE_PAID_SMS";

    @Test
    @DisplayName("SOLAPI를 통해 실제 SMS 한 건을 발송한다")
    void sendsOneRealSms() throws Exception {
        assertThat(System.getenv("SOLAPI_LIVE_TEST_CONFIRM"))
                .as("실제 발송을 승인하려면 SOLAPI_LIVE_TEST_CONFIRM을 설정해야 합니다.")
                .isEqualTo(SEND_CONFIRMATION);

        String recipient = requiredEnvironmentVariable("SOLAPI_LIVE_TEST_RECIPIENT");
        String apiKey = credential("SOLAPI_API_KEY", "solapi.api-key");
        String apiSecret = credential("SOLAPI_API_SECRET", "solapi.api-secret");
        String senderNumber = credential("SOLAPI_SENDER_NUMBER", "solapi.sender-number");

        DefaultMessageService messageService = SolapiClient.INSTANCE.createInstance(
                apiKey,
                apiSecret
        );
        SolapiMessageClient client = new SolapiSdkMessageClient(messageService, senderNumber);

        SolapiSendResponse response = client.send(new SolapiSmsRequest(
                recipient,
                "[동아리움] SOLAPI 실제 발송 테스트입니다.",
                null,
                "solapi-live-send-test",
                "solapi-live-send-test"
        ));

        assertThat(response.groupId()).isNotBlank();
        assertThat(response.messageId()).isNotBlank();
        assertThat(response.statusCode()).isNotBlank();
    }

    private String credential(String environmentName, String yamlPropertyName) throws Exception {
        String environmentValue = System.getenv(environmentName);
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue;
        }

        String yamlValue = applicationYamlResolver().getProperty(yamlPropertyName);
        assertThat(yamlValue)
                .as("%s 환경변수 또는 application.yml의 %s 설정이 필요합니다.",
                        environmentName, yamlPropertyName)
                .isNotBlank();
        return yamlValue;
    }

    private String requiredEnvironmentVariable(String name) {
        String value = System.getenv(name);
        assertThat(value)
                .as("%s 환경변수가 필요합니다.", name)
                .isNotBlank();
        return value;
    }

    private PropertySourcesPropertyResolver applicationYamlResolver() throws Exception {
        YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
        List<PropertySource<?>> loadedSources = loader.load(
                "solapi-live-test",
                new ClassPathResource("application.yml")
        );
        MutablePropertySources propertySources = new MutablePropertySources();
        loadedSources.forEach(propertySources::addLast);
        return new PropertySourcesPropertyResolver(propertySources);
    }
}
