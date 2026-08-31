---
type: quickstart
title: Quickstart
description: 동아리움(Dongarium) 백엔드가 무엇을 하는 서비스인지, 로컬에서 어떻게 띄우는지, 그리고 질문 종류별로 어떤 위키 페이지를 봐야 하는지 안내하는 진입점 문서.
tags: [quickstart, onboarding, task-routing]
verified:
  - by: openwiki/0.4.3
    at: 2026-08-31T14:35:03.507Z
sources:
  - id: openwiki-source-18fdeaac7d12c7b7f20e3a74
    resource: repo://build.gradle
  - id: openwiki-source-23775c3de52f3ab95a13cb8b
    resource: repo://README.md
generated: { by: "claude-code", at: "2026-08-31T14:35:03.507Z" }
---

# Quickstart

**동아리움(Dongarium)**은 대학 동아리의 신입 부원 모집 전 과정 — 지원폼 구성, 지원서 접수, 서류/면접 전형,
합격 발표, 지원자 통계 — 을 관리하는 Spring Boot 3.5.5 / Java 21 백엔드다(`build.gradle`). 카카오 OAuth2
로그인, JWT 인증, MySQL(운영)/H2(로컬·테스트), Redis(캐시·실시간 집계), AWS S3(파일 저장)를 사용하며,
GitHub Actions로 Docker 이미지를 빌드해 EC2에 배포한다.

## 로컬에서 띄우기

```bash
git clone https://github.com/kakao-tech-campus-3rd-step3/Team18_BE.git
cd Team18_BE
docker run -d --name dongarium-redis -p 6379:6379 redis:7-alpine
./gradlew bootRun
```

기본 프로필은 H2 인메모리 DB로 동작하며(`ddl-auto: create`), `http://localhost:8080/swagger-ui/index.html`에서
API 명세를 확인할 수 있다. 카카오 로그인, 메일 발송, S3 등 외부 연동 값은 환경변수 또는 로컬 전용
`src/main/resources/application.yml`(git에 커밋되지 않음)로 주입한다.

```bash
SPRING_PROFILES_ACTIVE=test ./gradlew clean build
```

로 테스트를 실행한다([[testing-strategy]] 참고).

## 무엇을 볼지 모를 때: 질문 유형별 안내

- **"이 프로젝트는 전체적으로 어떻게 구성돼 있나?"** → [[overview]](계층 구조, 패키지 분리, 프로필,
  전역 설정 클래스)
- **"Club/Application/ClubMember 같은 엔티티들이 서로 어떻게 연결되나?"** → [[domain-model]]
- **"예외를 던지면 어떻게 HTTP 응답이 되나?"** → [[error-handling]]
- **"카카오 로그인은 어떻게 동작하고 토큰은 어떻게 관리되나?"** → [[auth-and-session]]
- **"지원서를 내면 무슨 일이 일어나고, 합격/불합격은 어떻게 처리되나?"** → [[recruitment-application]]
- **"전형 결과 이메일은 언제, 어떻게 발송되나?"** → [[notification-emails]]
- **"실시간 인기 동아리는 어떻게 계산되나?"** → [[club-popularity]]
- **"지원자 통계는 어떻게 집계되고, 왜 어떤 경우엔 안 보이나?"** → [[statistics]]
- **"이미지·첨부파일은 어디에 저장되나?"** → [[file-storage]]
- **"배포는 어떻게 되고, 장애는 어떻게 알림이 오나?"** → [[deployment-and-observability]]
- **"테스트는 어떻게 짜여 있나?"** → [[testing-strategy]]

## 저장소 구조 한눈에

```
src/main/java/.../backend_server
├── domain   # club, clubApplyForm, application, answer, comment, clubMember,
│            # clubPopularity, clubReview, statistics, auth, email, notices,
│            # files, user, activity — 도메인별 controller/dto/entity/repository/service
└── global   # config, security, exception, scheduler, service, converter, util
```

브랜치 전략은 `main`(배포) / `develop`(통합, PR 기본 대상) / `{type}/{작업}#{이슈번호}`(작업 브랜치)이며,
모든 PR은 `develop` 대상 CI(빌드 & 테스트, [[deployment-and-observability]])를 통과해야 한다.
