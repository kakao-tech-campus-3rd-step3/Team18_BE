<div align="center">

# 🏫 동아리움 (Dongarium)

**대학 동아리 지원 · 관리 플랫폼 백엔드**

동아리 모집 공고 등록부터 지원서 접수, 서류/면접 전형, 합격 발표, 지원자 통계까지<br/>
동아리 운영의 전 과정을 하나로 관리하는 서비스의 백엔드 서버입니다.

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)](#-기술-스택)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-6DB33F?logo=springboot&logoColor=white)](#-기술-스택)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white)](#-기술-스택)
[![Redis](https://img.shields.io/badge/Redis-7-DC382D?logo=redis&logoColor=white)](#-기술-스택)
[![Kakao](https://img.shields.io/badge/Kakao%20OAuth-FFCD00?logo=kakaotalk&logoColor=black)](#-기술-스택)
[![AWS](https://img.shields.io/badge/AWS-EC2%20%7C%20S3%20%7C%20ECR-FF9900?logo=amazonaws&logoColor=white)](#-기술-스택)
[![Nginx](https://img.shields.io/badge/Nginx-reverse%20proxy-009639?logo=nginx&logoColor=white)](#-기술-스택)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)](#-기술-스택)
[![OpenWiki](https://img.shields.io/badge/Docs-OpenWiki-8A2BE2?logo=readthedocs&logoColor=white)](openwiki/quickstart.md)

**[🌐 서비스 바로가기](https://dongarium.co.kr)** · **[📑 API 문서](#-api-문서)** · **[🚀 시작하기](#-시작하기)**

</div>

---

## 📌 목차

- [소개](#-소개)
- [빠른 시작](#-빠른-시작)
- [주요 기능](#-주요-기능)
- [기술 스택](#-기술-스택)
- [아키텍처](#-아키텍처)
- [시작하기](#-시작하기)
- [API 문서](#-api-문서)
- [브랜치 & 커밋 전략](#-브랜치--커밋-전략)
- [프로젝트 구조](#-프로젝트-구조)
- [팀 소개](#-팀-소개)

## 📖 소개

**동아리움**은 대학 동아리의 신입 부원 모집 과정을 온라인으로 통합 관리할 수 있도록 돕는 플랫폼입니다.
운영진은 지원폼을 자유롭게 구성하고 지원자를 심사·관리할 수 있으며, 지원자는 카카오 로그인만으로 여러 동아리에 간편하게 지원할 수 있습니다.

- 🌐 서비스: [dongarium.co.kr](https://dongarium.co.kr)
- 🖥️ 프론트엔드 레포: [Team18_FE](https://github.com/kakao-tech-campus-3rd-step3/Team18_FE)

## ⚡ 빠른 시작

가장 빠르게 로컬에서 띄워보는 방법입니다. 상세 설정은 [시작하기](#-시작하기) 참고.

```bash
git clone https://github.com/kakao-tech-campus-3rd-step3/Team18_BE.git
cd Team18_BE
docker run -d --name dongarium-redis -p 6379:6379 redis:7-alpine
./gradlew bootRun
```

→ `http://localhost:8080/swagger-ui/index.html` 에서 API 확인

## ✨ 주요 기능

- **동아리 지원 관리**: 동아리별 커스텀 지원폼(질문/답변) 생성 및 지원서 접수
- **전형 프로세스**: 서류 → 면접 → 최종 합격/불합격 단계별 상태 관리
- **동아리원 관리**: 지원자의 동아리원 전환, 재지원 제한 등 멤버십 상태 관리
- **알림**: 전형 결과를 전형 단계별 템플릿 이메일로 자동 발송
- **카카오 소셜 로그인**: OAuth2 기반 인증, JWT 발급 및 Redis 기반 Refresh Token/로그아웃 블랙리스트 관리
- **동아리 후기**: 동아리원 대상 동아리 후기 작성/조회
- **지원자 통계**: 성별·학과·입학년도 등 다각도의 지원자 통계 집계 (재식별 방지를 위한 최소 공개 기준 적용, Redis 캐시)
- **동아리 인기도**: 조회수·체류시간 기반 실시간 인기 동아리 집계 (Redis + Lua 스크립트)
- **파일 업로드**: 동아리 이미지/지원서 첨부파일 AWS S3 업로드
- **모니터링**: Actuator + Prometheus + Grafana + Loki 기반 운영 모니터링, Discord Webhook 알림 연동

## 🛠 기술 스택

| 분류 | 스택 |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 3.5.5, Spring Data JPA, Spring Security, Spring Validation |
| Auth | OAuth2 Client (Kakao), JWT (jjwt) |
| Database | MySQL 8.0 (운영), H2 (로컬/테스트) |
| Cache / Realtime | Redis 7 — Refresh Token/블랙리스트, 동아리 인기도 집계, 통계 캐시 |
| Web Server | Nginx (리버스 프록시, HTTPS 종료) |
| Infra | Docker, Docker Compose, AWS EC2, AWS S3, AWS ECR |
| API 문서 | Springdoc OpenAPI (Swagger UI) |
| 문서화 | OpenWiki (저장소 위키 자동 생성/갱신) |
| Monitoring | Actuator, Prometheus, Grafana, Loki, Promtail, Discord Webhook |
| Test | JUnit5, Spring Security Test, Testcontainers (LocalStack), k6 (부하 테스트) |
| CI/CD | GitHub Actions → Amazon ECR → EC2 (SSH 배포) |
| 기타 | Spring Retry(AOP 재시도), Apache POI(엑셀), Lua(Redis 스크립트) |

## 🏗 아키텍처

```
                          Client (Web)
                               │ HTTPS
                               ▼
                             Nginx   ← 리버스 프록시 / TLS 종료
                ┌────────────────┴─────────────────┐
       dongarium.co.kr                    monitor.dongarium.co.kr
                │                                   │
                ▼                                   ▼
   ┌────────────────────────────┐              ┌─────────────┐
   │      Spring Boot App        │              │   Grafana    │
   │      (Docker, :8080)        │              └──────┬──────┘
   └──┬──────┬──────┬──────┬─────┘                     │
      │      │      │      │                     ┌─────┴─────┐
      ▼      ▼      ▼      ▼                  Prometheus     Loki ◄── Promtail
    MySQL  Redis   S3   Kakao OAuth2                ▲          (컨테이너 로그 수집)
                          (외부)                     │
                                       App의 `/actuator/prometheus` 스크레이핑
```

- 에러/알림: 로그백(Discord Appender), Grafana Alerting → Discord Webhook

- **배포 파이프라인**: `develop` 브랜치에 push되면 GitHub Actions가 빌드 → Docker 이미지 생성 → Amazon ECR push → SSH로 EC2 접속 후 `docker compose pull && up`으로 무중단 재기동합니다. (`.github/workflows/deploy-with-docker.yml`)
- **패키지 구조**는 도메인 기준으로 분리되어 있습니다. (`club`, `clubApplyForm`, `application`, `clubMember`, `clubReview`, `statistics`, `auth`, `email` 등)
- Nginx 설정은 EC2 호스트에서 직접 관리되며 이 저장소에는 포함되어 있지 않습니다.

## 🚀 시작하기

### 사전 요구 사항

- JDK 21
- Docker & Docker Compose (MySQL, Redis 로컬 구동용)

### 1. 저장소 클론

```bash
git clone https://github.com/kakao-tech-campus-3rd-step3/Team18_BE.git
cd Team18_BE
```

### 2. 로컬 인프라 실행 (선택)

로컬 기본 프로필은 H2 인메모리 DB로 동작하지만, Redis 관련 기능(인기도 집계, 토큰 저장 등)을 확인하려면 Redis가 필요합니다.

```bash
docker run -d --name dongarium-redis -p 6379:6379 redis:7-alpine
```

### 3. 환경변수 설정

카카오 로그인, 메일 발송, AWS S3 등 외부 연동 값은 환경변수 또는 로컬 전용 `src/main/resources/application.yml`(`.gitignore`에 등록되어 저장소에는 커밋되지 않음)로 주입합니다. 필요한 주요 값은 아래와 같습니다.

```
KAKAO_CLIENT_ID
KAKAO_CLIENT_SECRET
JWT_SECRET
SMTP_HOST / SMTP_PORT / SMTP_USERNAME / SMTP_PASSWORD
AWS_ACCESS_KEY / AWS_SECRET_KEY
S3_BUCKET / S3_BUCKET_ATTACHMENTS
```

> ⚠️ 팀 공유 시크릿은 노션/1Password 등 별도 채널로 전달됩니다. 저장소에 실제 값을 직접 커밋하지 마세요.

### 4. 애플리케이션 실행

```bash
./gradlew bootRun
```

기본 프로필로 실행하면 `http://localhost:8080`에서 서버가 뜨고, H2 콘솔은 `/h2-console`에서 확인할 수 있습니다.

### 5. 테스트 실행

```bash
SPRING_PROFILES_ACTIVE=test ./gradlew clean build
```

## 📑 API 문서

서버 실행 후 Swagger UI에서 전체 API 명세를 확인할 수 있습니다.

```
http://localhost:8080/swagger-ui/index.html
```

## 🌿 브랜치 & 커밋 전략

- `main` : 배포 브랜치
- `develop` : 통합 개발 브랜치 (PR은 기본적으로 이 브랜치를 대상으로 생성, push 시 자동 배포)
- `{type}/{작업-내용}#{이슈번호}` : 기능/작업 브랜치 (예: `feat/club-review#123`, `fix/apply-form-validation#456`)

이슈 템플릿(`.github/ISSUE_TEMPLATE`)에는 `feature` / `bug` / `refactor` / `question` 타입이 있으며, PR은 [PR 템플릿](.github/PULL_REQUEST_TEMPLATE.md)에 맞춰 작성합니다. 모든 PR은 `develop` 대상 CI(빌드 & 테스트)를 통과해야 합니다.

## 📂 프로젝트 구조

```
src/main/java/com/kakaotech/team18/backend_server
├── domain
│   ├── application       # 지원서
│   ├── auth               # 인증/인가 (OAuth2, JWT, Redis)
│   ├── club                # 동아리
│   ├── clubApplyForm      # 지원폼
│   ├── clubMember         # 동아리원
│   ├── clubPopularity     # 동아리 인기도
│   ├── clubReview          # 동아리 후기
│   ├── email               # 전형 결과 메일 발송
│   ├── statistics          # 지원자 통계
│   └── user                 # 사용자
└── global
    ├── config              # Security, Swagger, Redis 등 설정
    ├── exception           # 공통 예외 처리
    ├── security             # 인증 필터, JWT 처리
    └── util                 # 공통 유틸
```

## 👥 팀 소개

카카오테크 캠퍼스 3기 Step3 **Team 18**

### Backend

<table>
  <tr>
    <td align="center">
      <a href="https://github.com/gary5876"><img src="https://github.com/gary5876.png" width="100" alt="gary5876" /><br /><sub><b>gary5876</b></sub></a>
    </td>
    <td align="center">
      <a href="https://github.com/ji-mim"><img src="https://github.com/ji-mim.png" width="100" alt="ji-mim" /><br /><sub><b>ji-mim</b></sub></a>
    </td>
    <td align="center">
      <a href="https://github.com/jnujh"><img src="https://github.com/jnujh.png" width="100" alt="jnujh" /><br /><sub><b>jnujh</b></sub></a>
    </td>
  </tr>
</table>

### Frontend ([Team18_FE](https://github.com/kakao-tech-campus-3rd-step3/Team18_FE))

<table>
  <tr>
    <td align="center">
      <a href="https://github.com/kanghaeun"><img src="https://github.com/kanghaeun.png" width="100" alt="kanghaeun" /><br /><sub><b>kanghaeun</b></sub></a>
    </td>
    <td align="center">
      <a href="https://github.com/ganimjeong"><img src="https://github.com/ganimjeong.png" width="100" alt="ganimjeong" /><br /><sub><b>ganimjeong</b></sub></a>
    </td>
    <td align="center">
      <a href="https://github.com/aaaaaattt"><img src="https://github.com/aaaaaattt.png" width="100" alt="aaaaaattt" /><br /><sub><b>aaaaaattt</b></sub></a>
    </td>
  </tr>
</table>

---

<div align="center">

Made with ❤️ by Team 18

</div>
