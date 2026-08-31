---
type: testing-guide
title: 테스트 전략
description: 도메인별 단위/서비스/컨트롤러 테스트 계층, Testcontainers 기반 Redis·MySQL·LocalStack S3 통합 테스트, WithMockCustomUser로 만든 Spring Security 인가 테스트, 동아리 인기도 k6 부하 테스트 스크립트를 설명한다.
tags: [testing, junit5, testcontainers, spring-security-test, k6, load-test]
verified:
  - by: openwiki/0.4.3
    at: 2026-08-31T14:35:03.507Z
sources:
  - id: openwiki-source-18fdeaac7d12c7b7f20e3a74
    resource: repo://build.gradle
  - id: openwiki-source-814c5da4e0b22c6170c28bdb
    resource: repo://scripts/load-test/club-popularity.js
  - id: openwiki-source-3583b3085053c4c870d9e5db
    resource: repo://src/test/java/com/kakaotech/team18/backend_server/domain/club/controller/ClubControllerAuthTest.java
  - id: openwiki-source-7a229656588ba10c4271134b
    resource: repo://src/test/java/com/kakaotech/team18/backend_server/domain/clubPopularity/redis/ClubPopularityRedisRepositoryIntegrationTest.java
  - id: openwiki-source-695633abcc19afe2d3e54865
    resource: repo://src/test/java/com/kakaotech/team18/backend_server/global/security/WithMockCustomUser.java
  - id: openwiki-source-9075de9f8b86b7aa64df2fa6
    resource: repo://src/test/java/com/kakaotech/team18/backend_server/global/security/WithMockCustomUserSecurityContextFactory.java
  - id: openwiki-source-ebf0e11c72cdd507dcff6b39
    resource: repo://src/test/java/com/kakaotech/team18/backend_server/global/service/S3IntegrationTest.java
generated: { by: "claude-code", at: "2026-08-31T14:35:03.507Z" }
---

# 테스트 전략

`src/test/java`는 `src/main/java`와 동일한 도메인별 패키지 구조(`domain/{club, application, clubPopularity,
...}/{controller, service, entity, repository}`)를 그대로 반영해, 각 프로덕션 클래스와 나란히 테스트를
배치한다. 빌드는 `SPRING_PROFILES_ACTIVE=test ./gradlew clean build`로 실행되며, `test` 프로필은 H2를
MySQL 호환 모드로 띄워 프로덕션 스키마와 최대한 가깝게 검증한다([[deployment-and-observability]] 참고).

## 컨트롤러 계층: MockMvc + 커스텀 인가 애노테이션

컨트롤러 테스트는 `@SpringBootTest(webEnvironment = MOCK)` + `@AutoConfigureMockMvc`로 실제
`SecurityConfig`를 그대로 통과시키면서 서비스 계층만 `@MockBean`으로 대체하는 방식을 쓴다(예:
`ClubControllerAuthTest`). 인가 시나리오는 `WithMockCustomUser`
(`src/test/java/.../global/security/WithMockCustomUser.java`)라는 커스텀 애노테이션으로 검증한다.
`WithMockCustomUserSecurityContextFactory`가 이 애노테이션의 `memberships`(예: `"1:CLUB_ADMIN"`)를 파싱해
JWT 인증 없이도 `PrincipalDetails`를 가진 `SecurityContext`를 직접 주입하므로, 각 역할(`CLUB_ADMIN`,
`CLUB_MEMBER` 등)별 접근 제어를 토큰 발급 과정 없이 테스트할 수 있다.

## Redis/DB 통합 테스트: Testcontainers

`spring-security-test`(Spring Security 테스트 지원)와 별개로, Redis Lua 스크립트나 배치 쿼리처럼 실제
인프라 동작에 의존하는 로직은 `org.testcontainers:junit-jupiter`/`org.testcontainers:localstack`
(`build.gradle`)으로 컨테이너를 띄워 검증한다.

- `ClubPopularityRedisRepositoryIntegrationTest`(`domain/clubPopularity/redis/`)는 Spring 컨텍스트 없이
  `GenericContainer<>("redis:7-alpine")`를 직접 띄우고 `LettuceConnectionFactory`/`StringRedisTemplate`을
  수동 구성해, `ClubPopularityRedisRepository`의 Lua 스크립트(조회수/heartbeat 기록, 집계, 복구)가 실제
  Redis에서 원자적으로 동작하는지 검증한다.
- `ClubViewBatchRepositoryMySqlIntegrationTest`(`domain/clubPopularity/repository/`)는 실제 MySQL
  컨테이너로 배치 upsert 쿼리를 검증한다.
- `S3IntegrationTest`(`global/service/S3IntegrationTest.java`)는 `LocalStackContainer`로 S3를 흉내 내
  `S3Service.upload`/`deleteFile`을 검증하도록 작성되어 있지만, 클래스 전체가 `@Disabled`이고 개별 테스트도
  `"LocalStack 환경 필요 - CI에서는 무시"` 사유로 비활성화되어 있어 **현재 CI에서는 실행되지 않는다**.

## 부하 테스트: k6 (`scripts/load-test/club-popularity.js`)

동아리 인기도 실시간 집계([[club-popularity]])는 기능 테스트만으로는 검증하기 어려운 동시성·부하 특성을
가지므로, 별도의 k6 스크립트로 로컬 폐기 가능 환경(운영 아님)에서 부하를 재현한다. 세 개의 시나리오를
동시에 돌린다.

- `detail_entry`: 동아리 상세 조회(`GET /api/clubs/{clubId}`, 404도 정상으로 취급)와 조회 기록
  (`POST /api/clubs/{clubId}/views`, 204 기대)을 `ramping-vus`로 점증시킨다.
- `popular_polling`: 인기 동아리 목록(`GET /api/clubs/popular`)을 일정 VU 수로 지속 폴링한다.
  `heartbeat`: 체류 신호(`POST /api/clubs/{clubId}/heartbeat`, 204 기대)를 지속적으로 보낸다.

`LOAD_TEST_SHORT=true`로 각 시나리오 구간을 5~20초로 단축해 연결 확인용 짧은 실행도 지원한다. 관찰 지표는
API p50/p95/p99·오류율, Redis 오류 및 pending 큐 크기·최고 대기 시간, DB flush 성공/실패 건수, 복구 소요
시간 등으로, [[club-popularity]]의 플러시·복구 메커니즘이 부하 아래에서도 상한 내에 머무는지 확인하는 데
쓰인다.
