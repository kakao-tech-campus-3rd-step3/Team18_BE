-- 지원자 통계(#335) 1단계: 성별 수집 + 학과 정규화
--
-- 대상: 운영(MySQL). prod 프로필은 ddl-auto: none 이므로 수동 실행이 필요하다.

-- 1. 성별 컬럼 추가.
--    반드시 NULL 허용으로 추가한다. 이 컬럼이 생기기 전에 접수된 지원자는 성별을 알 수 없고,
--    NOT NULL로 만들면 기존 행에 임의의 기본값이 들어가 통계가 왜곡된다.
--    값은 Enum 이름(MALE / FEMALE)으로 저장된다. @Enumerated(EnumType.STRING)
ALTER TABLE users
    ADD COLUMN gender VARCHAR(20) NULL COMMENT '성별(MALE/FEMALE). NULL은 미입력';

-- 2. 기존 학과 데이터의 공백 제거.
--    애플리케이션은 이제 앞뒤·중간 공백을 모두 제거해 저장하므로, 기존 행도 같은 규칙으로 맞춰야
--    '컴퓨터 공학과'와 '컴퓨터공학과'가 서로 다른 버킷으로 집계되지 않는다.
--    일반 공백 외에 탭·개행·전각 공백(U+3000)까지 제거한다.
UPDATE users
SET department = REPLACE(
                     REPLACE(
                             REPLACE(
                                     REPLACE(department, CHAR(9), ''),
                                     CHAR(10), ''),
                             CHAR(13), ''),
                     ' ', '')
WHERE department IS NOT NULL;

UPDATE users
SET department = REPLACE(department, CONVERT(UNHEX('E38080') USING utf8mb4), '')
WHERE department IS NOT NULL;

-- 3. 공백만으로 이루어져 있던 학과는 '미입력'으로 통일한다.
UPDATE users
SET department = '미입력'
WHERE department IS NULL
   OR department = '';
