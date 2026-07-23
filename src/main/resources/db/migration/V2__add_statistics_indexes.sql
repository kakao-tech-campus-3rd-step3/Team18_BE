-- 지원자 통계(#335) 5단계: 집계 쿼리용 인덱스
--
-- 대상: 운영(MySQL). prod 프로필은 ddl-auto: none 이므로 수동 실행이 필요하다.

-- 모든 dimension 집계는 지원폼으로 지원서를 먼저 좁힌 뒤 users를 조인한다.
-- application.club_apply_form_id 는 FK라 MySQL(InnoDB)이 인덱스를 자동 생성하므로 추가하지 않는다.

-- 학과 집계는 users.department 로 GROUP BY 한다. 인덱스가 없으면 조인 결과 전체를 임시 테이블에 모아
-- 정렬해야 한다. 지원자 규모가 커지면 이 쿼리가 가장 먼저 느려진다.
CREATE INDEX idx_users_department ON users (department);
