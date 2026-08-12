package com.kakaotech.team18.backend_server.domain.notification.sms;

import org.springframework.stereotype.Component;

@Component
public class SmsMessagePolicy {

    static final int SMS_MAX_BYTES = 90;
    static final int LMS_MAX_BYTES = 2_000;
    private static final String LMS_SUBJECT = "동아리 지원 결과 안내";
    private static final String KOREAN_MOBILE_PATTERN = "01[016789][0-9]{7,8}";

    public PreparedSmsMessage prepare(String rawRecipient, String text) {
        String recipient = normalizeRecipient(rawRecipient);
        validateText(text);

        int byteLength = calculateCarrierBytes(text);
        if (byteLength > LMS_MAX_BYTES) {
            throw new InvalidSmsMessageException(
                    "SMS_MESSAGE_TOO_LONG",
                    "문자 본문은 통신사 기준 2,000바이트를 초과할 수 없습니다."
            );
        }

        SmsMessageType type = byteLength <= SMS_MAX_BYTES
                ? SmsMessageType.SMS
                : SmsMessageType.LMS;
        return new PreparedSmsMessage(
                recipient,
                text,
                type == SmsMessageType.LMS ? LMS_SUBJECT : null,
                byteLength,
                type
        );
    }

    private String normalizeRecipient(String rawRecipient) {
        if (rawRecipient == null || rawRecipient.isBlank()) {
            throw new InvalidSmsMessageException(
                    "SMS_RECIPIENT_REQUIRED",
                    "문자 수신번호가 없습니다."
            );
        }
        String normalized = rawRecipient.replace("-", "").replace(" ", "");
        if (!normalized.matches(KOREAN_MOBILE_PATTERN)) {
            throw new InvalidSmsMessageException(
                    "SMS_RECIPIENT_INVALID",
                    "문자 수신번호는 대한민국 휴대전화 번호 형식이어야 합니다."
            );
        }
        return normalized;
    }

    private void validateText(String text) {
        if (text == null || text.isBlank()) {
            throw new InvalidSmsMessageException(
                    "SMS_MESSAGE_REQUIRED",
                    "문자 본문이 없습니다."
            );
        }
    }

    /** SOLAPI 안내 기준인 영문 1바이트, 한글 등 비 ASCII 문자 2바이트로 계산합니다. */
    private int calculateCarrierBytes(String text) {
        return text.codePoints()
                .map(codePoint -> codePoint <= 0x7F ? 1 : 2)
                .sum();
    }
}
