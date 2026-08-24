-- 운영 DB 수동 적용용 SQL입니다.
-- 애플리케이션 배포 전에 MySQL 8.x 환경에서 먼저 적용합니다.

ALTER TABLE notification_delivery
    ADD COLUMN failure_alerted_at DATETIME(6) NULL AFTER unknown_at,
    ADD INDEX idx_notification_delivery_failure_alert (status, failure_alerted_at, notification_delivery_id);
