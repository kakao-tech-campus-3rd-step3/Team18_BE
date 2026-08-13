package com.kakaotech.team18.backend_server.domain.notification.solapi;

import com.kakaotech.team18.backend_server.domain.notification.sms.InvalidSmsMessageException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SolapiRecipientAllowlistTest {

    @Test
    void normalizesConfiguredNumbersAndAllowsOnlyListedRecipient() {
        SolapiRecipientAllowlist allowlist = new SolapiRecipientAllowlist(
                true,
                "010-1234-5678, 010 9999 8888"
        );

        allowlist.validate("01012345678");
        assertThat(allowlist.size()).isEqualTo(2);
        assertThatThrownBy(() -> allowlist.validate("01011112222"))
                .isInstanceOfSatisfying(InvalidSmsMessageException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo("SMS_RECIPIENT_NOT_ALLOWED"));
    }

    @Test
    void emptyEnabledAllowlistBlocksEveryActualSend() {
        SolapiRecipientAllowlist allowlist = new SolapiRecipientAllowlist(true, "");

        assertThatThrownBy(() -> allowlist.validate("01012345678"))
                .isInstanceOf(InvalidSmsMessageException.class);
    }

    @Test
    void disabledAllowlistAllowsProductionRecipients() {
        SolapiRecipientAllowlist allowlist = SolapiRecipientAllowlist.allowAll();

        allowlist.validate("01012345678");
        assertThat(allowlist.isEnabled()).isFalse();
    }

    @Test
    void rejectsMalformedConfiguredNumberAtStartup() {
        assertThatThrownBy(() -> new SolapiRecipientAllowlist(true, "02-123-4567"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SOLAPI_RECIPIENT_ALLOWLIST");
    }
}
