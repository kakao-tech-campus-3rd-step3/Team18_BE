-- 지원자 통계(#335) 1단계: 성별 수집
--
-- 대상: 운영(MySQL). prod 프로필은 ddl-auto: none 이므로 수동 실행이 필요하다.

-- 성별 컬럼 추가.
-- 반드시 NULL 허용으로 추가한다. 이 컬럼이 생기기 전에 접수된 지원자는 성별을 알 수 없고,
-- NOT NULL로 만들면 기존 행에 임의의 기본값이 들어가 통계가 왜곡된다.
-- 값은 Enum 이름(MALE / FEMALE)으로 저장된다. @Enumerated(EnumType.STRING)
ALTER TABLE users
    ADD COLUMN gender VARCHAR(20) NULL COMMENT '성별(MALE/FEMALE). NULL은 미입력';
