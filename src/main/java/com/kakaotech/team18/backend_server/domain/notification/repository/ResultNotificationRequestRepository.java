package com.kakaotech.team18.backend_server.domain.notification.repository;

import com.kakaotech.team18.backend_server.domain.notification.entity.ResultNotificationRequest;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResultNotificationRequestRepository extends JpaRepository<ResultNotificationRequest, Long> {

    Optional<ResultNotificationRequest> findByClubIdAndIdempotencyKey(Long clubId, String idempotencyKey);
}
