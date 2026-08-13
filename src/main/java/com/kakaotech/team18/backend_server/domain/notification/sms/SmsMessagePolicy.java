package com.kakaotech.team18.backend_server.domain.notification.sms;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SmsMessagePolicy {

    static final int SMS_MAX_BYTES = 90;
    static final int LMS_MAX_BYTES = 2_000;
    private static final String LMS_SUBJECT = "동아리 지원 결과 안내";
    private static final String KOREAN_MOBILE_PATTERN = "01[016789][0-9]{7,8}";
    private static final Charset CARRIER_CHARSET = Charset.forName("EUC-KR");

    private final BigDecimal smsEstimatedCost;
    private final BigDecimal lmsEstimatedCost;

    public SmsMessagePolicy() {
        this(BigDecimal.ZERO, BigDecimal.ZERO);
    }

    @Autowired
    public SmsMessagePolicy(
            @Value("${notification.sms.estimated-cost.sms:0}") BigDecimal smsEstimatedCost,
            @Value("${notification.sms.estimated-cost.lms:0}") BigDecimal lmsEstimatedCost
    ) {
        if (smsEstimatedCost.signum() < 0 || lmsEstimatedCost.signum() < 0) {
            throw new IllegalArgumentException("문자 예상 단가는 0 이상이어야 합니다.");
        }
        this.smsEstimatedCost = smsEstimatedCost;
        this.lmsEstimatedCost = lmsEstimatedCost;
    }

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
                type,
                type == SmsMessageType.SMS ? smsEstimatedCost : lmsEstimatedCost
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

    private int calculateCarrierBytes(String text) {
        try {
            ByteBuffer encoded = CARRIER_CHARSET.newEncoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .encode(CharBuffer.wrap(text));
            return encoded.remaining();
        } catch (CharacterCodingException exception) {
            throw new InvalidSmsMessageException(
                    "SMS_UNSUPPORTED_CHARACTER",
                    "문자 본문에 EUC-KR로 전송할 수 없는 문자가 포함되어 있습니다."
            );
        }
    }
}
