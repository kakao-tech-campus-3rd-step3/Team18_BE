-- 개별 SOLAPI 발송의 SMS/LMS 유형과 설정 단가 기준 예상비용 스냅샷입니다.

ALTER TABLE notification_delivery
    ADD COLUMN message_type VARCHAR(20) NULL AFTER provider_status_code,
    ADD COLUMN estimated_cost DECIMAL(14, 4) NULL AFTER message_type;
