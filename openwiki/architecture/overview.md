---
type: architecture-concept
title: 시스템 아키텍처 개요
description: 동아리움 백엔드의 계층 구조, 도메인 기준 패키지 구성, 외부 의존성(MySQL/Redis/S3/Kakao), 프로필별 설정, 그리고 전역 Spring 설정 클래스들의 역할을 정리한다.
tags: [architecture, spring-boot, configuration, infrastructure]
verified:
  - by: openwiki/0.4.3
    at: 2026-08-31T14:35:03.507Z
sources:
  - id: openwiki-source-b79fbbd921df689b4bbdc82f
    resource: repo://docker-compose.yml
  - id: openwiki-source-bb1ebe868e35e9e500714501
    resource: repo://Dockerfile
  - id: openwiki-source-23775c3de52f3ab95a13cb8b
    resource: repo://README.md
  - id: openwiki-source-a3578093cd9103cd2bae2bba
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/global/config/AppConfig.java
  - id: openwiki-source-d79e6692a75213fb3d7ffac8
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/global/config/RedisConfig.java
  - id: openwiki-source-25a4d576f010c324afd19aed
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/global/config/RepositoryConfig.java
  - id: openwiki-source-091557d9ed4a92af14504e3b
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/Team18BeApplication.java
generated: { by: "claude-code", at: "2026-08-31T14:35:03.507Z" }
---

# 시스템 아키텍처 개요

동아리움(Dongarium)은 단일 Spring Boot 모놀리스로, 대학 동아리의 신입 부원 모집 전 과정(지원폼 구성 → 지원서
접수 → 서류/면접 전형 → 합격 발표 → 통계)을 관리한다. 진입점은
`Team18BeApplication`(`src/main/java/com/kakaotech/team18/backend_server/Team18BeApplication.java`)이며,
`@EnableScheduling`으로 스케줄러 기반 배치 작업(동아리 인기도 집계, 고아 이미지 정리 등)을 최상위에서 활성화한다.

## 패키지 구조: 도메인 기준 분리

`src/main/java/.../backend_server` 아래는 `domain`과 `global` 두 축으로 나뉜다.

- `domain/*`: 각 비즈니스 영역(`club`, `clubApplyForm`, `formQuestion`, `application`, `answer`, `comment`,
  `clubMember`, `clubReview`, `clubPopularity`, `statistics`, `auth`, `email`, `notices`, `files`, `user`,
  `activity`)이 자신만의 `controller`/`dto`/`entity`/`repository`/`service` 하위 패키지를 갖는 수직 분리
  구조다. 엔티티 간 관계는 [[domain-model]]에서 다룬다.
- `global/*`: 도메인을 가로지르는 공통 관심사 — `config`(Spring 설정), `security`(JWT/필터),
  `exception`(예외 계층, [[error-handling]] 참고), `scheduler`(주기 작업), `service`(S3 등 공용 서비스),
  `converter`, `annotation`, `util`.

## 외부 의존성과 프로필

애플리케이션은 3개 Spring 프로필(기본/`test`/`prod`)로 환경을 구분한다.

- **기본 프로필(로컬)**: H2 인메모리 DB(`ddl-auto: create`), `/h2-console` 활성화. Redis는 로컬 `localhost:6379`를
  가정한다.
- **`test` 프로필(CI)**: H2를 MySQL 호환 모드(`MODE=MYSQL`)로 띄우고 `ddl-auto: create-drop`을 사용해 매 실행마다
  스키마를 새로 만든다.
- **`prod` 프로필**: 실제 MySQL 8.0(`jdbc:mysql://db:3306/dongarium-db`)에 연결하고 `ddl-auto: none`(스키마
  마이그레이션을 애플리케이션이 직접 하지 않음)으로 운영한다. Actuator의 `prometheus`/`metrics` 엔드포인트를
  노출한다.

세 프로필 모두 아래 외부 시스템에 의존한다.

- **MySQL**: 운영 데이터 저장(prod), 로컬/CI는 H2로 대체.
- **Redis**: Refresh Token/로그아웃 블랙리스트([[auth-and-session]]), 동아리 인기도 실시간 집계
  ([[club-popularity]]), 지원자 통계 캐시([[statistics]]).
- **Kakao OAuth2**: 소셜 로그인의 인가 코드 교환 및 사용자 정보 조회 대상.
- **AWS S3**: 동아리 이미지/지원서 첨부파일 저장([[file-storage]]).
- **SMTP(Gmail)**: 전형 결과 알림 메일 발송([[notification-emails]]).

## 전역 설정 클래스(`global/config`)의 역할

- `AppConfig`: 카카오 등 외부 서버 호출용 `RestClient` 빈을 등록하며, `connect-timeout`/`read-timeout`을 설정해
  외부 서비스 지연이 애플리케이션 전체로 전파되지 않도록 한다.
- `RedisConfig`: Lettuce의 기본 커맨드 타임아웃(1분)을 최대 3초로 강제해, Redis 장애 시 요청이 오래 대기하지
  않고 빠르게 우회 경로로 전환되게 한다.
- `RepositoryConfig`: `@EnableJpaRepositories`와 `@EnableRedisRepositories`를 같은 `domain` 베이스 패키지에
  적용하되, `RefreshTokenRepository`만 Redis Repository로 분류하고 나머지는 모두 JPA Repository로 분류하도록
  포함/제외 필터를 명시적으로 나눈다.
- `JpaAuditingConfig`: `@EnableJpaAuditing`으로 `BaseEntity`의 `@CreatedDate`/`@LastModifiedDate` 자동 기록을
  활성화한다.
- `SecurityConfig`: JWT 기반 무상태 인증 필터 체인을 구성한다. 자세한 흐름은 [[auth-and-session]] 참고.
- `AsyncConfig`: `@EnableAsync`로 비동기 이벤트 리스너(이메일 발송 등)를 활성화한다.
- `S3Config`/`RetryConfig`/`MetricsConfig`/`SwaggerConfig`: 각각 AWS S3 클라이언트, Spring Retry(AOP 재시도),
  Micrometer 메트릭, Springdoc OpenAPI(Swagger UI) 설정을 담당한다.

## 배포 이미지와 컨테이너 구성

`Dockerfile`은 `eclipse-temurin:21-jdk-alpine` 기반으로 빌드된 JAR를 `--spring.profiles.active=prod`로
실행하며, `/actuator/health`를 헬스체크 엔드포인트로 사용한다. `docker-compose.yml`은 `db`(MySQL 8.0),
`redis`, `app` 세 서비스를 하나의 `dongarium-net` 네트워크로 묶고, `app`은 `db`/`redis`의 헬스체크가
`service_healthy`가 될 때까지 시작을 지연한다. 배포 파이프라인 세부 사항은 [[deployment-and-observability]]를
참고.
