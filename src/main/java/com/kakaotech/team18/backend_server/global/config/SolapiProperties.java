package com.kakaotech.team18.backend_server.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "solapi")
public class SolapiProperties {

    private boolean enabled;
    private String apiKey;
    private String apiSecret;
    private String senderNumber;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }

    public String getSenderNumber() {
        return senderNumber;
    }

    public void setSenderNumber(String senderNumber) {
        this.senderNumber = senderNumber;
    }

    public void validateEnabledConfiguration() {
        requireText(apiKey, "SOLAPI_API_KEY");
        requireText(apiSecret, "SOLAPI_API_SECRET");
        requireText(senderNumber, "SOLAPI_SENDER_NUMBER");
        if (!senderNumber.matches("[0-9]{8,11}")) {
            throw new IllegalStateException(
                    "SOLAPI_SENDER_NUMBER는 하이픈 없이 8~11자리 숫자여야 합니다."
            );
        }
    }

    private void requireText(String value, String environmentName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "SOLAPI가 활성화되었지만 " + environmentName + " 값이 없습니다."
            );
        }
    }
}
