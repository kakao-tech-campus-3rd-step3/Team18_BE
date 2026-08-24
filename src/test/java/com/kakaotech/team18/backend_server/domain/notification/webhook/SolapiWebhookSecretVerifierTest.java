package com.kakaotech.team18.backend_server.domain.notification.webhook;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SolapiWebhookSecretVerifierTest {

    @Test
    void verifiesSha1HashWithoutExposingRawSecret() {
        SolapiWebhookSecretVerifier verifier = new SolapiWebhookSecretVerifier("test-secret");

        assertThat(verifier.matches("fe1bae27cb7c1fb823f496f286e78f1d2ae87734")).isTrue();
        assertThat(verifier.matches("wrong")).isFalse();
        assertThat(verifier.matches(null)).isFalse();
    }
}
