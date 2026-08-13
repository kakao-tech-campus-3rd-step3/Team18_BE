package com.kakaotech.team18.backend_server.domain.notification.webhook;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SolapiWebhookSecretVerifier {

    private final byte[] expectedHash;

    public SolapiWebhookSecretVerifier(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("SOLAPI Webhook Secret이 없습니다.");
        }
        expectedHash = sha1Hex(secret).getBytes(StandardCharsets.US_ASCII);
    }

    public boolean matches(String receivedHash) {
        return receivedHash != null && MessageDigest.isEqual(
                expectedHash,
                receivedHash.toLowerCase().getBytes(StandardCharsets.US_ASCII)
        );
    }

    private String sha1Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-1")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-1 알고리즘을 사용할 수 없습니다.", exception);
        }
    }
}
