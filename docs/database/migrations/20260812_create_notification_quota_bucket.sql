-- SOLAPI 과금 API의 시간당 호출 횟수를 여러 서버 인스턴스가 공유하기 위한 버킷입니다.

CREATE TABLE notification_quota_bucket
(
    notification_quota_bucket_id BIGINT      NOT NULL AUTO_INCREMENT,
    version                      BIGINT      NOT NULL DEFAULT 0,
    channel                      VARCHAR(20) NOT NULL,
    bucket_started_at            DATETIME(6) NOT NULL,
    request_count                INT         NOT NULL DEFAULT 0,
    created_at                   DATETIME(6) NOT NULL,
    last_modified_at             DATETIME(6) NOT NULL,
    PRIMARY KEY (notification_quota_bucket_id),
    CONSTRAINT uk_notification_quota_channel_hour UNIQUE (channel, bucket_started_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
