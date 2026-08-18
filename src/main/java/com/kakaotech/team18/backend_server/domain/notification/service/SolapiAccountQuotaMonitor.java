package com.kakaotech.team18.backend_server.domain.notification.service;

import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiAccountQuotaResponse;
import com.kakaotech.team18.backend_server.domain.notification.solapi.SolapiMessageClient;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
public class SolapiAccountQuotaMonitor {

    private final SolapiMessageClient messageClient;
    private final int applicationDailyLimit;
    private final AtomicBoolean mismatchAlerted = new AtomicBoolean();
    private final AtomicBoolean lookupFailureAlerted = new AtomicBoolean();

    public SolapiAccountQuotaMonitor(
            SolapiMessageClient messageClient,
            int applicationDailyLimit
    ) {
        if (applicationDailyLimit <= 0) {
            throw new IllegalArgumentException("애플리케이션 일일 SOLAPI 호출 한도는 0보다 커야 합니다.");
        }
        this.messageClient = messageClient;
        this.applicationDailyLimit = applicationDailyLimit;
    }

    @Scheduled(fixedDelayString = "${notification.solapi-quota-monitor.scheduler-delay-ms:3600000}")
    public void checkQuota() {
        try {
            SolapiAccountQuotaResponse response = messageClient.getAccountQuota();
            lookupFailureAlerted.set(false);
            if (applicationDailyLimit > response.dailyQuota()) {
                if (mismatchAlerted.compareAndSet(false, true)) {
                    log.error(
                            "Application daily SOLAPI limit exceeds account quota: "
                                    + "applicationLimit={} accountQuota={} autoAdjustment={}",
                            applicationDailyLimit,
                            response.dailyQuota(),
                            response.autoAdjustment()
                    );
                }
                return;
            }
            if (mismatchAlerted.getAndSet(false)) {
                log.info(
                        "Application daily SOLAPI limit is within account quota: "
                                + "applicationLimit={} accountQuota={}",
                        applicationDailyLimit,
                        response.dailyQuota()
                );
            }
        } catch (RuntimeException exception) {
            if (lookupFailureAlerted.compareAndSet(false, true)) {
                log.error("SOLAPI account quota lookup failed", exception);
            }
        }
    }

    boolean isMismatchAlerted() {
        return mismatchAlerted.get();
    }

    boolean isLookupFailureAlerted() {
        return lookupFailureAlerted.get();
    }
}
