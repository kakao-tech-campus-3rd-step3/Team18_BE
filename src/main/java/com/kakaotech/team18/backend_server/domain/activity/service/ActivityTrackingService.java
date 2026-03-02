package com.kakaotech.team18.backend_server.domain.activity.service;

import com.kakaotech.team18.backend_server.domain.activity.entity.ActivityDaily;
import com.kakaotech.team18.backend_server.domain.activity.repository.ActivityDailyRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityTrackingService {

    private final ActivityDailyRepository activityDailyRepository;

    @Transactional
    public void track(String anonymousId, Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        if (userId != null) {
            ActivityDaily row = activityDailyRepository.findByActivityDateAndUserId(today, userId)
                    .orElse(null);
            if (row == null) {
                activityDailyRepository.save(ActivityDaily.forUser(today, userId, now));
                return;
            }
            row.touch(now);
            activityDailyRepository.save(row);
            return;
        }
        if (anonymousId == null || anonymousId.isBlank()) {
            log.warn("Skip activity tracking: anonymousId is missing");
            return;
        }
        ActivityDaily row = activityDailyRepository.findByActivityDateAndAnonymousId(today, anonymousId)
                .orElse(null);
        if (row == null) {
            activityDailyRepository.save(ActivityDaily.forAnonymous(today, anonymousId, now));
            return;
        }
        row.touch(now);
        activityDailyRepository.save(row);
    }
}
