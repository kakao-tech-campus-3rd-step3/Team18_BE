-- 시간 단위 SOLAPI 버킷을 시간·일·월 사용량 버킷으로 확장합니다.
-- 20260812_create_notification_quota_bucket.sql 적용 후 실행합니다.

ALTER TABLE notification_quota_bucket
    DROP INDEX uk_notification_quota_channel_hour,
    ADD COLUMN period VARCHAR(20) NOT NULL DEFAULT 'HOUR' AFTER channel,
    ADD COLUMN sms_count INT NOT NULL DEFAULT 0 AFTER request_count,
    ADD COLUMN lms_count INT NOT NULL DEFAULT 0 AFTER sms_count,
    ADD COLUMN estimated_cost DECIMAL(14, 4) NOT NULL DEFAULT 0 AFTER lms_count,
    ADD COLUMN highest_alerted_percent INT NOT NULL DEFAULT 0 AFTER estimated_cost,
    ADD CONSTRAINT uk_notification_quota_channel_period_start
        UNIQUE (channel, period, bucket_started_at);
