-- 장기 PENDING 작업의 개발자 알림 중복을 방지하는 시각입니다.
-- 20260812_add_notification_failure_alert.sql 적용 후 실행합니다.

ALTER TABLE notification_delivery
    ADD COLUMN pending_alerted_at DATETIME(6) NULL AFTER failure_alerted_at,
    ADD INDEX idx_notification_delivery_pending_alert
        (status, pending_alerted_at, created_at, notification_delivery_id);
