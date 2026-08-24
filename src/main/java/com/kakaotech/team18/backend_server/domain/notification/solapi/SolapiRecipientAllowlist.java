package com.kakaotech.team18.backend_server.domain.notification.solapi;

import com.kakaotech.team18.backend_server.domain.notification.sms.InvalidSmsMessageException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class SolapiRecipientAllowlist {

    private final boolean enabled;
    private final Set<String> allowedRecipients;

    public SolapiRecipientAllowlist(boolean enabled, String configuredRecipients) {
        this.enabled = enabled;
        this.allowedRecipients = parse(configuredRecipients);
    }

    public static SolapiRecipientAllowlist allowAll() {
        return new SolapiRecipientAllowlist(false, "");
    }

    public void validate(String normalizedRecipient) {
        if (enabled && !allowedRecipients.contains(normalizedRecipient)) {
            throw new InvalidSmsMessageException(
                    "SMS_RECIPIENT_NOT_ALLOWED",
                    "현재 환경에서 허용되지 않은 문자 수신번호입니다."
            );
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int size() {
        return allowedRecipients.size();
    }

    private Set<String> parse(String configuredRecipients) {
        if (configuredRecipients == null || configuredRecipients.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(configuredRecipients.split(","))
                .map(String::strip)
                .filter(value -> !value.isBlank())
                .map(value -> value.replace("-", "").replace(" ", ""))
                .peek(this::validateConfiguredNumber)
                .collect(Collectors.toUnmodifiableSet());
    }

    private void validateConfiguredNumber(String number) {
        if (!number.matches("01[016789][0-9]{7,8}")) {
            throw new IllegalStateException(
                    "SOLAPI_RECIPIENT_ALLOWLIST에 올바르지 않은 휴대전화 번호가 있습니다."
            );
        }
    }
}
