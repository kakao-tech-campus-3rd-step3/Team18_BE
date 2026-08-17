-- 결과 알림 민감정보의 보관기간 만료 비식별화 시각입니다.
-- 실제 비식별화 스케줄러는 팀의 보관기간 합의 후에만 활성화합니다.

ALTER TABLE notification_delivery
    ADD COLUMN redacted_at DATETIME(6) NULL AFTER pending_alerted_at,
    ADD INDEX idx_notification_delivery_retention
        (status, redacted_at, created_at, notification_delivery_id);
