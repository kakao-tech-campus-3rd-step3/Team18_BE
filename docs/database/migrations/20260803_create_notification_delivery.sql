-- 운영 DB 수동 적용용 SQL입니다.
-- 애플리케이션 배포 전에 MySQL 8.x 환경에서 먼저 적용합니다.

CREATE TABLE notification_delivery
(
    notification_delivery_id BIGINT       NOT NULL AUTO_INCREMENT,
    version                  BIGINT       NOT NULL DEFAULT 0,
    club_id                  BIGINT       NOT NULL,
    user_id                  BIGINT       NOT NULL,
    application_id           BIGINT       NOT NULL,
    channel                  VARCHAR(20)  NOT NULL,
    result_type              VARCHAR(40)  NOT NULL,
    status                   VARCHAR(40)  NOT NULL,
    recipient_address        VARCHAR(320) NOT NULL,
    message_subject          VARCHAR(255) NULL,
    message_body             TEXT         NOT NULL,
    attempt_count            INT          NOT NULL DEFAULT 0,
    next_attempt_at          DATETIME(6)  NOT NULL,
    last_attempt_at          DATETIME(6)  NULL,
    accepted_at              DATETIME(6)  NULL,
    sent_at                  DATETIME(6)  NULL,
    failed_at                DATETIME(6)  NULL,
    unknown_at               DATETIME(6)  NULL,
    provider_group_id        VARCHAR(100) NULL,
    provider_message_id      VARCHAR(100) NULL,
    provider_status_code     VARCHAR(40)  NULL,
    provider_error_code      VARCHAR(100) NULL,
    last_error_message       VARCHAR(1000) NULL,
    created_at               DATETIME(6)  NOT NULL,
    last_modified_at         DATETIME(6)  NOT NULL,
    PRIMARY KEY (notification_delivery_id),
    CONSTRAINT uk_notification_delivery_provider_message UNIQUE (provider_message_id),
    INDEX idx_notification_delivery_dispatch (status, next_attempt_at, notification_delivery_id),
    INDEX idx_notification_delivery_club_created (club_id, created_at),
    INDEX idx_notification_delivery_created (created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
