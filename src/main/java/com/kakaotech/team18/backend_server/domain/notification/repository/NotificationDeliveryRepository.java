package com.kakaotech.team18.backend_server.domain.notification.repository;

import com.kakaotech.team18.backend_server.domain.notification.entity.NotificationDelivery;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select delivery from NotificationDelivery delivery where delivery.id = :id")
    Optional<NotificationDelivery> findByIdForUpdate(@Param("id") Long id);
}
