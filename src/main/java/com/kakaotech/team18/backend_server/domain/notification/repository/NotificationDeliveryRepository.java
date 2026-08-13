package com.kakaotech.team18.backend_server.domain.notification.repository;

import com.kakaotech.team18.backend_server.domain.notification.entity.NotificationDelivery;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationDeliveryStatus;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select delivery from NotificationDelivery delivery where delivery.id = :id")
    Optional<NotificationDelivery> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select delivery from NotificationDelivery delivery where delivery.providerMessageId = :messageId")
    Optional<NotificationDelivery> findByProviderMessageIdForUpdate(
            @Param("messageId") String messageId
    );

    @Query("""
            select delivery.id
            from NotificationDelivery delivery
            where delivery.status = com.kakaotech.team18.backend_server.domain.notification.type.NotificationDeliveryStatus.PENDING
              and delivery.nextAttemptAt <= :now
            order by delivery.id
            """)
    List<Long> findDueDeliveryIds(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("""
            select delivery.id
            from NotificationDelivery delivery
            where delivery.status = com.kakaotech.team18.backend_server.domain.notification.type.NotificationDeliveryStatus.SENDING
              and delivery.lastAttemptAt <= :cutoff
            order by delivery.id
            """)
    List<Long> findStaleSendingDeliveryIds(
            @Param("cutoff") LocalDateTime cutoff,
            Pageable pageable
    );

    @Query("""
            select delivery.id
            from NotificationDelivery delivery
            where delivery.status = com.kakaotech.team18.backend_server.domain.notification.type.NotificationDeliveryStatus.ACCEPTED
              and delivery.nextAttemptAt <= :now
            order by delivery.id
            """)
    List<Long> findDueAcceptedDeliveryIds(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("""
            select delivery.id
            from NotificationDelivery delivery
            where delivery.status in :statuses
              and delivery.failureAlertedAt is null
            order by delivery.id
            """)
    List<Long> findUnalertedFailureIds(
            @Param("statuses") List<NotificationDeliveryStatus> statuses,
            Pageable pageable
    );

    long countByStatus(NotificationDeliveryStatus status);
}
