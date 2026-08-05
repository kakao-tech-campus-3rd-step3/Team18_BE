-- 실시간 인기 동아리(#338): DB 조회 기록 저장
--
-- 대상: 운영(MySQL). prod 프로필은 ddl-auto: none 이므로 수동 실행이 필요하다.
-- 실행 전 club.club_id 타입과 기존 테이블 존재 여부를 확인한다.

CREATE TABLE club_view (
    id BIGINT NOT NULL AUTO_INCREMENT,
    club_id BIGINT NOT NULL,
    user_id BIGINT NULL,
    anonymous_identity VARBINARY(512) NULL,
    last_viewed_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_club_view_user UNIQUE (club_id, user_id),
    CONSTRAINT uk_club_view_anonymous UNIQUE (club_id, anonymous_identity),
    CONSTRAINT fk_club_view_club
        FOREIGN KEY (club_id) REFERENCES club (club_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 롤백이 필요한 경우 데이터 보존 여부를 확인한 뒤 운영 승인 후 실행한다.
-- DROP TABLE club_view;
