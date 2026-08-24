package com.kakaotech.team18.backend_server.domain.notification.entity;

import com.kakaotech.team18.backend_server.domain.BaseEntity;
import com.kakaotech.team18.backend_server.domain.application.entity.Stage;
import com.kakaotech.team18.backend_server.domain.notification.type.NotificationRequestStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "result_notification_request",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_result_notification_request_club_key",
                columnNames = {"club_id", "idempotency_key"}
        )
)
public class ResultNotificationRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_notification_request_id")
    private Long id;

    @Column(name = "club_id", nullable = false)
    private Long clubId;

    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;

    @Column(name = "request_fingerprint", nullable = false, length = 64)
    private String requestFingerprint;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage", nullable = false, length = 20)
    private Stage stage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationRequestStatus status;

    @Column(name = "success")
    private Boolean success;

    private ResultNotificationRequest(
            Long clubId,
            String idempotencyKey,
            String requestFingerprint,
            Stage stage
    ) {
        this.clubId = clubId;
        this.idempotencyKey = idempotencyKey;
        this.requestFingerprint = requestFingerprint;
        this.stage = stage;
        this.status = NotificationRequestStatus.PROCESSING;
    }

    public static ResultNotificationRequest processing(
            Long clubId,
            String idempotencyKey,
            String requestFingerprint,
            Stage stage
    ) {
        return new ResultNotificationRequest(clubId, idempotencyKey, requestFingerprint, stage);
    }

    public boolean hasSameFingerprint(String fingerprint) {
        return Objects.equals(requestFingerprint, fingerprint);
    }

    public void complete(boolean success) {
        if (status != NotificationRequestStatus.PROCESSING) {
            throw new IllegalStateException("이미 완료된 결과 알림 요청입니다.");
        }
        this.status = NotificationRequestStatus.COMPLETED;
        this.success = success;
    }
}
