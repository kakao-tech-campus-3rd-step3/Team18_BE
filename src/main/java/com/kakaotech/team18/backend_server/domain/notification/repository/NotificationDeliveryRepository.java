package com.kakaotech.team18.backend_server.domain.notification.repository;

import com.kakaotech.team18.backend_server.domain.notification.entity.NotificationDelivery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, Long> {
}
