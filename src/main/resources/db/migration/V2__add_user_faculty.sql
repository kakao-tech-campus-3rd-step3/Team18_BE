-- 지원자 통계(#335): 학부 수집
--
-- 대상: 운영(MySQL). prod 프로필은 ddl-auto: none 이므로 수동 실행이 필요하다.

-- 학부 컬럼 추가. NULL 허용으로 추가한다.
-- 이 컬럼이 생기기 전에 접수된 지원자나 비지원 경로(카카오·동아리원)로 생성된 User는 학부를 알 수 없다.
-- 값은 Enum 이름(NURSING/BUSINESS/.../ETC)으로 저장된다. @Enumerated(EnumType.STRING)
ALTER TABLE users
    ADD COLUMN faculty VARCHAR(40) NULL COMMENT '학부(Faculty enum 이름). NULL은 미입력';
