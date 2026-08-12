-- 결과 이메일의 회신 주소를 발송 시점까지 보존하기 위한 스냅샷 컬럼입니다.
-- 20260803_add_notification_idempotency.sql 적용 후 실행합니다.

ALTER TABLE notification_delivery
    ADD COLUMN reply_to_address VARCHAR(320) NULL AFTER recipient_address;
