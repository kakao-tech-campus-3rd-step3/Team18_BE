package com.kakaotech.team18.backend_server.domain.notification.util;

import com.kakaotech.team18.backend_server.domain.application.dto.ApplicationApprovedRequestDto;
import com.kakaotech.team18.backend_server.domain.application.entity.Stage;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class NotificationRequestFingerprintGenerator {

    private NotificationRequestFingerprintGenerator() {
    }

    public static String generate(
            Long clubId,
            Stage stage,
            ApplicationApprovedRequestDto request
    ) {
        String channels = request.channels().stream()
                .map(Enum::name)
                .sorted()
                .reduce((left, right) -> left + "," + right)
                .orElse("");

        StringBuilder canonicalRequest = new StringBuilder();
        append(canonicalRequest, clubId == null ? null : clubId.toString());
        append(canonicalRequest, stage == null ? null : stage.name());
        append(canonicalRequest, request.message());
        append(canonicalRequest, channels);

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(
                    digest.digest(canonicalRequest.toString().getBytes(StandardCharsets.UTF_8))
            );
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }

    private static void append(StringBuilder target, String value) {
        if (value == null) {
            target.append("-1:");
            return;
        }
        target.append(value.length()).append(':').append(value);
    }
}
