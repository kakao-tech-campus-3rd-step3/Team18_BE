package com.kakaotech.team18.backend_server.global.scheduler;

import com.kakaotech.team18.backend_server.domain.club.service.OrphanImageCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrphanImageCleanupScheduler {

    private final OrphanImageCleanupService cleanupService;

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupOrphanImages() {
        log.info("Starting orphan image cleanup process...");
        int deleted = cleanupService.cleanupOrphanImagesLogic();
        log.info("Cleanup completed. Deleted {} orphan images.", deleted);
    }
}