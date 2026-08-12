package com.kakaotech.team18.backend_server.domain.notification.sms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class SmsMessagePolicyTest {

    private final SmsMessagePolicy policy = new SmsMessagePolicy();

    @Test
    void normalizesKoreanMobileNumberAndKeepsShortMessageAsSms() {
        PreparedSmsMessage prepared = policy.prepare(
                "010-1234-5678",
                "합격을 축하드립니다."
        );

        assertThat(prepared.recipient()).isEqualTo("01012345678");
        assertThat(prepared.type()).isEqualTo(SmsMessageType.SMS);
        assertThat(prepared.subject()).isNull();
    }

    @Test
    void classifiesMessageOverNinetyCarrierBytesAsLms() {
        PreparedSmsMessage prepared = policy.prepare(
                "01012345678",
                "가".repeat(46)
        );

        assertThat(prepared.byteLength()).isEqualTo(92);
        assertThat(prepared.type()).isEqualTo(SmsMessageType.LMS);
        assertThat(prepared.subject()).isEqualTo("동아리 지원 결과 안내");
    }

    @Test
    void rejectsInvalidMobileNumber() {
        assertThatThrownBy(() -> policy.prepare("02-123-4567", "결과 안내"))
                .isInstanceOfSatisfying(InvalidSmsMessageException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo("SMS_RECIPIENT_INVALID"));
    }

    @Test
    void rejectsMessageOverLmsLimit() {
        assertThatThrownBy(() -> policy.prepare("01012345678", "가".repeat(1_001)))
                .isInstanceOfSatisfying(InvalidSmsMessageException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo("SMS_MESSAGE_TOO_LONG"));
    }
}
