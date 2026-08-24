package com.kakaotech.team18.backend_server.domain.notification.service;

import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiBalanceResponse;
import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiMessageClient;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
public class SolapiBalanceMonitor {

    private final SolapiMessageClient messageClient;
    private final BigDecimal lowBalanceThreshold;
    private final AtomicBoolean lowBalanceAlerted = new AtomicBoolean();
    private final AtomicBoolean lookupFailureAlerted = new AtomicBoolean();

    public SolapiBalanceMonitor(
            SolapiMessageClient messageClient,
            BigDecimal lowBalanceThreshold
    ) {
        if (lowBalanceThreshold == null || lowBalanceThreshold.signum() <= 0) {
            throw new IllegalArgumentException("SOLAPI 잔액 경고 기준은 0보다 커야 합니다.");
        }
        this.messageClient = messageClient;
        this.lowBalanceThreshold = lowBalanceThreshold;
    }

    @Scheduled(fixedDelayString = "${notification.solapi-balance.scheduler-delay-ms:3600000}")
    public void checkBalance() {
        try {
            SolapiBalanceResponse response = messageClient.getBalance();
            lookupFailureAlerted.set(false);
            BigDecimal available = response.availableAmount();
            if (available.compareTo(lowBalanceThreshold) < 0) {
                if (lowBalanceAlerted.compareAndSet(false, true)) {
                    log.error(
                            "SOLAPI available balance is below threshold: available={} threshold={}",
                            available,
                            lowBalanceThreshold
                    );
                }
                return;
            }
            if (lowBalanceAlerted.getAndSet(false)) {
                log.info("SOLAPI available balance recovered above threshold: available={} threshold={}",
                        available, lowBalanceThreshold);
            }
        } catch (RuntimeException exception) {
            if (lookupFailureAlerted.compareAndSet(false, true)) {
                log.error("SOLAPI balance lookup failed", exception);
            }
        }
    }

    boolean isLowBalanceAlerted() {
        return lowBalanceAlerted.get();
    }

    boolean isLookupFailureAlerted() {
        return lookupFailureAlerted.get();
    }
}
