package com.kakaotech.team18.backend_server.domain.activity.repository;

import com.kakaotech.team18.backend_server.domain.activity.entity.ActivityDaily;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityDailyRepository extends JpaRepository<ActivityDaily, Long> {
    Optional<ActivityDaily> findByActivityDateAndUserId(LocalDate activityDate, Long userId);

    Optional<ActivityDaily> findByActivityDateAndAnonymousId(LocalDate activityDate, String anonymousId);
}
