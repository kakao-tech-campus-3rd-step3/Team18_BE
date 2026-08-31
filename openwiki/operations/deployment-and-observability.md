---
type: operations-guide
title: 배포 파이프라인과 모니터링
description: develop 브랜치 push부터 Docker 이미지 빌드, ECR 푸시, EC2 docker compose 재기동까지의 CI/CD 흐름과 Prometheus/Grafana/Loki 기반 모니터링, Discord 알림 구성을 설명한다.
tags: [ci-cd, github-actions, deployment, observability, prometheus, grafana, discord]
verified:
  - by: openwiki/0.4.3
    at: 2026-08-31T14:35:03.507Z
sources:
  - id: openwiki-source-c82bb4445673bbe2883441ba
    resource: repo://.github/workflows/ci-on-pr.yml
  - id: openwiki-source-87b050ccf7cba44437f818f5
    resource: repo://.github/workflows/deploy-with-docker.yml
  - id: openwiki-source-a22e73cc1658173cc70a4441
    resource: repo://docker-compose.monitoring.yml
  - id: openwiki-source-7c7f0572827acdd84852ff33
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/global/config/MetricsConfig.java
  - id: openwiki-source-0da8c8f041a8df89bb8481e7
    resource: repo://src/main/resources/logback-spring.xml
generated: { by: "claude-code", at: "2026-08-31T14:35:03.507Z" }
---

# 배포 파이프라인과 모니터링

## CI: PR 빌드/테스트 (`ci-on-pr.yml`)

`develop`을 대상으로 한 PR이 열리거나 갱신될 때(`opened`/`synchronize`/`reopened`) 실행된다. `redis:7-alpine`을
GitHub Actions 서비스 컨테이너로 띄운 뒤, 시크릿 값들로 `application-test.yml`을 임시 생성하고
`SPRING_PROFILES_ACTIVE=test ./gradlew clean build`로 빌드와 테스트를 함께 수행한다. 같은 PR의 최신 커밋
기준으로만 실행되도록 `concurrency` 그룹으로 이전 실행을 취소한다. 포크 저장소에서 온 PR은
`github.event.pull_request.head.repo.full_name == github.repository` 조건으로 실행에서 제외해, 외부
기여자의 PR이 리포지토리 시크릿에 접근하지 못하게 한다.

## CD: EC2 배포 (`deploy-with-docker.yml`)

`develop` 브랜치에 push되면(또는 수동 `workflow_dispatch`) 다음 순서로 무중단에 가까운 배포를 수행한다.

1. **빌드**: `APPLICATION_YML` 시크릿 전체를 `src/main/resources/application.yml`로 복원한 뒤,
   `SPRING_PROFILES_ACTIVE=test ./gradlew clean build -x test`로 테스트를 건너뛰고 JAR만 빌드한다(테스트는
   이미 CI 단계에서 검증됨).
2. **이미지 푸시**: `Dockerfile`로 이미지를 빌드해 Amazon ECR(`dongarium-server`)에 `latest` 태그로 푸시한다.
3. **파일 전달**: `docker-compose.yml`, `docker-compose.monitoring.yml`, `monitoring/` 디렉터리를
   `appleboy/scp-action`으로 EC2의 `~/dongarium/current`에 업로드한다.
4. **원격 배포**: `appleboy/ssh-action`으로 EC2에 접속해 ECR credential helper를 설정하고, 모든 런타임
   시크릿(DB/카카오/JWT/SMTP/Redis/S3/Discord)을 `~/dongarium/shared/.env`에 `install -m 600`으로 안전하게
   생성한 뒤 `current/.env`로 심볼릭 링크한다. 이후 `docker compose down` → `docker compose up -d
   --remove-orphans`로 애플리케이션 컨테이너를 재기동하고, 이어서 `docker-compose.monitoring.yml`로 모니터링
   스택을 올린다.

`cd-on-merge.yml.disabled`는 비활성화된 워크플로 파일로, 현재는 `deploy-with-docker.yml`만 배포에 사용된다.

## 모니터링 스택 (`docker-compose.monitoring.yml`)

애플리케이션과 별도 compose 파일로 `dongarium-net`(애플리케이션 스택과 공유하는 외부 네트워크)에 4개
컨테이너를 구성한다.

- **Prometheus**: 애플리케이션의 `/actuator/prometheus`를 스크레이핑하고, `monitoring/prometheus/`
  아래 alert 규칙(`club-popularity-alerts.yml`)을 로드한다. 데이터 보존은 15일 또는 10GB 중 먼저 도달하는
  기준까지다.
- **Grafana**: `GF_UNIFIED_ALERTING_ENABLED=true`로 통합 알림을 사용하며, `DISCORD_WEBHOOK_URL` 환경변수를
  통해 알림을 Discord로 전달한다. 익명 접근과 자체 회원가입은 비활성화되어 있다.
- **Loki + Promtail**: Promtail이 호스트의 Docker 컨테이너 로그(`/var/lib/docker/containers`)와 Docker
  소켓을 읽어 Loki로 전달해, Grafana에서 로그를 함께 조회할 수 있게 한다.

## 애플리케이션 레벨 관측성

- `MetricsConfig`(`global/config/MetricsConfig.java`)는 `TimedAspect`/`CountedAspect`로 `@Timed`/`@Counted`
  메서드 레벨 메트릭을 지원하고, `dongarium.users.total`/`dongarium.clubs.total`/`dongarium.applications.total`
  세 개의 Gauge 메트릭을 등록해 전체 사용자·동아리·지원서 수를 실시간으로 노출한다.
- `logback-spring.xml`은 프로필별로 로그 출력을 분기한다. 기본/`test` 프로필은 콘솔 로그만 남기지만, `prod`
  프로필에서는 `com.github.napstr.logback.DiscordAppender`를 `AsyncAppender`로 감싸고
  `ThresholdFilter(level=ERROR)`를 적용해, ERROR 레벨 로그만 비동기로 Discord 웹훅에 전송한다. 이 방식으로
  운영 중 발생하는 에러를 애플리케이션 스레드를 막지 않고 실시간 알림으로 받는다.
- `prod` 프로필은 Actuator에서 `health,info,prometheus,metrics` 엔드포인트만 노출하고
  `health.show-details: never`로 상세 헬스 정보의 외부 노출을 막는다.
