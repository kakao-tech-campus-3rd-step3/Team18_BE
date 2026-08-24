package com.kakaotech.team18.backend_server.domain.notification.service;

import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiBalanceResponse;
import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiClientException;
import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiMessageClient;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class SolapiBalanceMonitorTest {

    private final SolapiMessageClient messageClient = mock(SolapiMessageClient.class);

    @Test
    void suppressesRepeatedLowBalanceAlertUntilBalanceRecovers() {
        given(messageClient.getBalance()).willReturn(
                balance("3000", "1000"),
                balance("3000", "1000"),
                balance("6000", "0"),
                balance("1000", "0")
        );
        SolapiBalanceMonitor monitor = new SolapiBalanceMonitor(
                messageClient,
                new BigDecimal("5000")
        );

        monitor.checkBalance();
        assertThat(monitor.isLowBalanceAlerted()).isTrue();
        monitor.checkBalance();
        assertThat(monitor.isLowBalanceAlerted()).isTrue();
        monitor.checkBalance();
        assertThat(monitor.isLowBalanceAlerted()).isFalse();
        monitor.checkBalance();
        assertThat(monitor.isLowBalanceAlerted()).isTrue();
    }

    @Test
    void suppressesLookupFailureUntilSuccessfulLookup() {
        given(messageClient.getBalance())
                .willThrow(SolapiClientException.unknown("LOOKUP_FAILED", "failed", null))
                .willThrow(SolapiClientException.unknown("LOOKUP_FAILED", "failed", null))
                .willReturn(balance("6000", "0"));
        SolapiBalanceMonitor monitor = new SolapiBalanceMonitor(
                messageClient,
                new BigDecimal("5000")
        );

        monitor.checkBalance();
        assertThat(monitor.isLookupFailureAlerted()).isTrue();
        monitor.checkBalance();
        assertThat(monitor.isLookupFailureAlerted()).isTrue();
        monitor.checkBalance();
        assertThat(monitor.isLookupFailureAlerted()).isFalse();
    }

    @Test
    void rejectsNonPositiveThreshold() {
        assertThatThrownBy(() -> new SolapiBalanceMonitor(messageClient, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private SolapiBalanceResponse balance(String balance, String point) {
        return new SolapiBalanceResponse(new BigDecimal(balance), new BigDecimal(point));
    }
}
