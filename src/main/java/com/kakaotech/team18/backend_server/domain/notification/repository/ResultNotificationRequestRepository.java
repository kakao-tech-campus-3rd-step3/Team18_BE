package com.kakaotech.team18.backend_server.domain.notification.repository;

import com.kakaotech.team18.backend_server.domain.notification.entity.ResultNotificationRequest;
import com.kakaotech.team18.backend_server.domain.notification.dto.ResultNotificationRequestSummary;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResultNotificationRequestRepository extends JpaRepository<ResultNotificationRequest, Long> {

    Optional<ResultNotificationRequest> findByClubIdAndIdempotencyKey(Long clubId, String idempotencyKey);

    @Query("""
            select new com.kakaotech.team18.backend_server.domain.notification.dto.ResultNotificationRequestSummary(
                request.id,
                request.idempotencyKey,
                request.stage,
                request.status,
                request.createdAt,
                count(delivery.id),
                sum(case when delivery.status in (
                    com.kakaotech.team18.backend_server.domain.notification.type.NotificationDeliveryStatus.PENDING,
                    com.kakaotech.team18.backend_server.domain.notification.type.NotificationDeliveryStatus.SENDING
                ) then 1L else 0L end),
                sum(case when delivery.status =
                    com.kakaotech.team18.backend_server.domain.notification.type.NotificationDeliveryStatus.ACCEPTED
                    then 1L else 0L end),
                sum(case when delivery.status =
                    com.kakaotech.team18.backend_server.domain.notification.type.NotificationDeliveryStatus.SENT
                    then 1L else 0L end),
                sum(case when delivery.status in (
                    com.kakaotech.team18.backend_server.domain.notification.type.NotificationDeliveryStatus.FAILED,
                    com.kakaotech.team18.backend_server.domain.notification.type.NotificationDeliveryStatus.PERMANENTLY_FAILED
                ) then 1L else 0L end),
                sum(case when delivery.status =
                    com.kakaotech.team18.backend_server.domain.notification.type.NotificationDeliveryStatus.UNKNOWN
                    then 1L else 0L end),
                sum(case when delivery.messageType =
                    com.kakaotech.team18.backend_server.domain.notification.sms.SmsMessageType.SMS
                    then 1L else 0L end),
                sum(case when delivery.messageType =
                    com.kakaotech.team18.backend_server.domain.notification.sms.SmsMessageType.LMS
                    then 1L else 0L end),
                coalesce(sum(delivery.estimatedCost), 0BD)
            )
            from ResultNotificationRequest request
            left join NotificationDelivery delivery
              on delivery.clubId = request.clubId
             and delivery.idempotencyKey = request.idempotencyKey
            where request.clubId = :clubId
            group by request.id, request.idempotencyKey, request.stage, request.status, request.createdAt
            order by request.createdAt desc, request.id desc
            """)
    List<ResultNotificationRequestSummary> findSummariesByClubId(
            @Param("clubId") Long clubId,
            Pageable pageable
    );
}
