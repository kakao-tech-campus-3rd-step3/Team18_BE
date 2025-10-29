package com.kakaotech.team18.backend_server.domain.club.eventListener;

import com.kakaotech.team18.backend_server.global.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClubImageDeletionListener {

    private final S3Service s3Service;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleClubImageDeletedEvent(ClubImageDeletedEvent event) {
        for (String url : event.getImageUrls()) {
            try {
                s3Service.deleteFile(url);
            } catch (Exception e) {
                log.warn("S3 삭제 실패 for clubId={}, url={} : {}", event.getClubId(), url, e.getMessage());
            }
        }
        log.info("S3 기존 이미지 삭제 완료 for clubId={}", event.getClubId());
    }
}