-- 지원자 통계(#335) 8단계: 확정 통계 스냅샷 테이블
--
-- 대상: 운영(MySQL). prod 프로필은 ddl-auto: none 이므로 수동 실행이 필요하다.
--
-- 불합격 지원서는 관리자가 단계를 전환할 때 삭제된다(ApplicationServiceImpl.sendPassFailMessage).
-- 지원폼과 지원자를 잇는 유일한 연결이 application 행이라, 삭제된 뒤에는 그 지원자의 성별/학과가
-- users에 남아 있어도 어느 지원폼에 지원했는지 알 수 없다. 그래서 삭제 직전에 집계 결과를 확정 저장한다.
--
-- payload에는 마스킹까지 끝난 공개용 응답(JSON)만 담긴다. 개별 지원자 정보는 저장하지 않는다.

CREATE TABLE club_apply_form_statistics_snapshot
(
    club_apply_form_statistics_snapshot_id BIGINT       NOT NULL AUTO_INCREMENT,
    club_apply_form_id                     BIGINT       NOT NULL,
    total_applicants                       BIGINT       NOT NULL,
    -- 집계 규칙 버전. 규칙이 바뀌었을 때 재계산 대상을 골라내기 위해 저장한다.
    rules_version                          VARCHAR(20)  NOT NULL,
    payload                                LONGTEXT     NOT NULL,
    created_at                             DATETIME(6)  NULL,
    last_modified_at                       DATETIME(6)  NULL,
    PRIMARY KEY (club_apply_form_statistics_snapshot_id),
    -- 지원폼당 스냅샷은 하나다. 첫 단계 전환 시점의 전체 지원자 분포가 확정본이며 덮어쓰지 않는다.
    CONSTRAINT uk_statistics_snapshot_club_apply_form UNIQUE (club_apply_form_id),
    CONSTRAINT fk_statistics_snapshot_club_apply_form
        FOREIGN KEY (club_apply_form_id) REFERENCES club_apply_form (club_apply_form_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
