package com.kakaotech.team18.backend_server.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiAccountQuotaResponse;
import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiClientException;
import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiMessageClient;
import org.junit.jupiter.api.Test;

class SolapiAccountQuotaMonitorTest {

    private final SolapiMessageClient messageClient = mock(SolapiMessageClient.class);

    @Test
    void suppressesRepeatedMismatchAlertUntilQuotaRecovers() {
        given(messageClient.getAccountQuota()).willReturn(
                quota(50),
                quota(50),
                quota(100),
                quota(40)
        );
        SolapiAccountQuotaMonitor monitor = new SolapiAccountQuotaMonitor(messageClient, 100);

        monitor.checkQuota();
        assertThat(monitor.isMismatchAlerted()).isTrue();
        monitor.checkQuota();
        assertThat(monitor.isMismatchAlerted()).isTrue();
        monitor.checkQuota();
        assertThat(monitor.isMismatchAlerted()).isFalse();
        monitor.checkQuota();
        assertThat(monitor.isMismatchAlerted()).isTrue();
    }

    @Test
    void suppressesLookupFailureUntilSuccessfulLookup() {
        given(messageClient.getAccountQuota())
                .willThrow(SolapiClientException.unknown("LOOKUP_FAILED", "failed", null))
                .willThrow(SolapiClientException.unknown("LOOKUP_FAILED", "failed", null))
                .willReturn(quota(100));
        SolapiAccountQuotaMonitor monitor = new SolapiAccountQuotaMonitor(messageClient, 100);

        monitor.checkQuota();
        assertThat(monitor.isLookupFailureAlerted()).isTrue();
        monitor.checkQuota();
        assertThat(monitor.isLookupFailureAlerted()).isTrue();
        monitor.checkQuota();
        assertThat(monitor.isLookupFailureAlerted()).isFalse();
    }

    @Test
    void rejectsNonPositiveApplicationLimit() {
        assertThatThrownBy(() -> new SolapiAccountQuotaMonitor(messageClient, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private SolapiAccountQuotaResponse quota(int dailyQuota) {
        return new SolapiAccountQuotaResponse(dailyQuota, false);
    }
}
