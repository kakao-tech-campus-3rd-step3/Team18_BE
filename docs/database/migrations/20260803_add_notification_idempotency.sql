-- 운영 DB 수동 적용용 SQL입니다.
-- 20260803_create_notification_delivery.sql 적용 후 실행합니다.
-- 4단계 전까지 notification_delivery 데이터가 생성되지 않는 것을 전제로 합니다.

CREATE TABLE result_notification_request
(
    result_notification_request_id BIGINT       NOT NULL AUTO_INCREMENT,
    club_id                       BIGINT       NOT NULL,
    idempotency_key               VARCHAR(100) NOT NULL,
    request_fingerprint           CHAR(64)     NOT NULL,
    stage                         VARCHAR(20)  NOT NULL,
    status                        VARCHAR(20)  NOT NULL,
    success                       BOOLEAN      NULL,
    created_at                    DATETIME(6)  NOT NULL,
    last_modified_at              DATETIME(6)  NOT NULL,
    PRIMARY KEY (result_notification_request_id),
    CONSTRAINT uk_result_notification_request_club_key UNIQUE (club_id, idempotency_key)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

ALTER TABLE notification_delivery
    ADD COLUMN idempotency_key VARCHAR(100) NOT NULL AFTER application_id,
    ADD CONSTRAINT uk_notification_delivery_idempotency_recipient
        UNIQUE (club_id, idempotency_key, user_id, channel, result_type);
