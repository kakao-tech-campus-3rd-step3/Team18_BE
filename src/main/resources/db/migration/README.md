# 운영 DB 마이그레이션

`prod` 프로필은 `spring.jpa.hibernate.ddl-auto: none`(`application.yml`)이므로 스키마가 자동으로 반영되지 않는다.
개발 프로필은 `create` / `create-drop`이라 마이그레이션을 빠뜨려도 로컬에서는 드러나지 않으니, **엔티티를 바꿀 때마다
여기에 대응하는 SQL을 반드시 추가한다.**

Flyway·Liquibase는 도입되어 있지 않다. 아래 파일은 **배포 시 운영 DB에 순서대로 직접 실행**한다.

| 순서 | 파일 | 내용 |
|---|---|---|
| 1 | `V1__add_user_gender_and_normalize_department.sql` | `users.gender` 컬럼 추가, 기존 `department` 공백 제거 |
| 2 | `V2__add_department_index.sql` | 학과 통계 집계용 인덱스 |
| 3 | `V3__create_club_apply_form_statistics_snapshot.sql` | 통계 스냅샷 테이블 |

실행 전 확인:

- 각 스크립트는 재실행해도 안전하도록 작성되어 있지 않다. 한 번만 실행한다.
- `V1`의 `UPDATE`는 전체 `users` 행을 훑는다. 지원자 규모가 크면 트래픽이 적은 시간대에 실행한다.
