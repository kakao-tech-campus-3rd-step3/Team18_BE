# 현재 AWS 운영 환경 조사 (2026-09-29)

GCP 이전(#387)을 위해 운영 환경을 직접 조사한 결과다. 조사는 모두 읽기 전용으로 진행했고,
비밀값은 담지 않는다. Terraform과 전환 절차를 만들 때의 근거 자료다.

조사 방법: EC2 SSH 접속(`docker`, `nginx -T`, `mysql`, `redis-cli`, 접근 로그), 외부에서의 HTTP 응답 확인, `whois`/`dig`.

## 1. 구성 한눈에 보기

```
사용자 → dongarium.co.kr (Route 53 → EC2 단일 IP)
         nginx (호스트에서 실행, Let's Encrypt)
         ├─ /api/                    → 127.0.0.1:8080 (백엔드 컨테이너)
         ├─ /swagger-ui, /v3/api-docs → 127.0.0.1:8080
         └─ /                        → CloudFront → S3 (프론트 SPA)
       monitor.dongarium.co.kr → 127.0.0.1:3000 (Grafana)
```

EC2 한 대에서 컨테이너 7개가 동작한다: 백엔드, MySQL, Redis, Prometheus, Grafana, Loki, promtail.
nginx는 컨테이너가 아니라 호스트에 설치되어 있다(`systemd`).

## 2. 서버 사양과 사용량

| 항목 | 값 |
|---|---|
| OS | Amazon Linux 2023 (커널 6.1) |
| 사양 | 2 vCPU / 1901MB RAM / 20GB 디스크 |
| 메모리 사용 | 1528MB / 1901MB, **swap 없음** |
| 디스크 사용 | 76% (`/var/lib/docker` 12GB, `/var/log` 924MB) |
| 컨테이너 메모리 | 백엔드 509MB, MySQL 412MB, Grafana 108MB, Loki 71MB, Prometheus 69MB, promtail 44MB, Redis 8MB |
| 컨테이너 CPU | 전부 1% 미만 |
| 연속 가동 | 383일 |

**CPU는 거의 쓰지 않고 메모리만 빠듯하다.** Cloud Run 사양과 예산을 정할 때의 기준이 된다.

## 3. 트래픽 (nginx 접근 로그)

| 항목 | 값 |
|---|---|
| 전체 요청 | 약 800건 (현재 로그 파일 기준) |
| 피크 | 375건/시간 (03시 UTC = 12시 KST) |
| 실제 API 호출 | `/api/clubs?category=ALL` 13건, `/api/clubs/popular` 10건 수준 |
| 봇 트래픽 | `/robots.txt`, `/sitemap.xml`, `/{id}.php`, `/wp-content/...` 등이 상당 비중 |
| 상태 코드 | 200: 382, 301: 302, 302: 47, 400: 31, 403: 21 |

## 4. nginx 라우팅 (LB로 옮길 규칙)

| 규칙 | 동작 |
|---|---|
| 80 | `301 https://dongarium.co.kr$request_uri` |
| `www` (443) | `301` non-www로 리다이렉트 (정규 호스트는 non-www) |
| `/api/` | `proxy_pass http://127.0.0.1:8080`, `X-Forwarded-*` 전달 |
| `/api/` CORS | Origin이 `https://(www.)?dongarium.co.kr`일 때 허용 헤더 추가, `OPTIONS`는 204 |
| `/swagger-ui`, `/v3/api-docs` | `proxy_pass http://127.0.0.1:8080` |
| `/` | `proxy_pass https://d3l4bprkd35agv.cloudfront.net` (Host 헤더를 CloudFront 도메인으로 교체) |
| `monitor.dongarium.co.kr` | `proxy_pass http://127.0.0.1:3000` (Grafana), 인증서 별도 |

- `client_max_body_size`가 설정되어 있지 않다 → nginx 기본값 1MB.
  앱은 파일 5MB·요청 50MB를 허용하므로 `/api/`로 들어오는 큰 업로드는 nginx에서 막힐 수 있다.
- 인증서는 Let's Encrypt 2장(`dongarium.co.kr`, `monitor.dongarium.co.kr`), `certbot-renew.timer`로 자동 갱신된다.

## 5. CloudFront의 실제 역할

외부에서 CloudFront 도메인으로 직접 요청해 확인한 결과, **백엔드와 연결되어 있지 않다.** S3 버킷만 바라본다.

| 요청 경로 | 응답 |
|---|---|
| `/`, `/index.html`, `/assets/*` | `server: AmazonS3`, `x-cache: Hit from cloudfront` |
| `/login`, `/nonexistent-page-xyz`, `/api/...`, `/actuator/...` | 모두 `index.html`을 200으로 반환 (SPA 폴백) |

즉 CloudFront가 하는 일은 **CDN 캐시 + 없는 경로를 `index.html`로 돌려주는 SPA 폴백**뿐이다.
GCP에서는 LB의 URL map, Cloud CDN, 사용자 지정 오류 응답으로 대체된다. 대응 서비스를 따로 만들 필요가 없다.

## 6. 데이터 저장소

| 항목 | 값 |
|---|---|
| MySQL | 8.0.43 (컨테이너), utf8mb4 / utf8mb4_unicode_ci, `system_time_zone=KST`, `max_connections=200` |
| DB 크기 | **4.5MB** (`dongarium-db`) |
| 행 수 상위 | `activity_daily` 12,387 / `club_member` 582 / `club_image` 405 / `users` 269 |
| 뷰·트리거·프로시저 | **0개** → 덤프 시 `DEFINER` 문제 없음. `db/migration/V3__create_club_view.sql`은 운영에 적용되지 않은 상태 |
| Redis | 7-alpine, 사용 메모리 1.65MB, 키 20개, **AOF 꺼짐**, `maxmemory` 제한 없음 |
| 포트 | MySQL 3306이 `0.0.0.0`으로 열려 있다 (보안 그룹에 의존) |

DB가 작아서 덤프와 import는 수 초면 끝난다. 전환 시 중단 시간은 대부분 DNS 전파가 차지한다.

## 7. 저장된 파일 URL 분포 (`club_image.image_url`)

| prefix | 건수 |
|---|---|
| `https://dongarium-club-image.s3.ap-northeast-2.amazonaws.com` | 391 |
| `https://cf-ea.everytime.kr` | 5 |
| `https://images.unsplash.com` | 2 |

S3가 아닌 외부 URL이 섞여 있으므로, 마이그레이션은 **S3 prefix만 교체**해야 한다. 전체 일괄 변환은 안 된다.
첨부파일은 `file` 테이블의 `object_uri`에 저장된다.

## 8. 저장소와 서버가 일치하는 것

| 항목 | 확인 |
|---|---|
| `docker-compose.yml`, `docker-compose.monitoring.yml` | md5 해시가 저장소와 동일 |
| `monitoring/` 13개 파일 | 서버 파일과 동일 (prometheus, loki, promtail, grafana provisioning) |
| Grafana 대시보드 6개, 데이터소스, 알림 | provisioning 파일로 관리 |
| Prometheus 알림 규칙 3개 | `ClubPopularityRedisRecordingFailureRateHigh`, `ClubPopularityPendingOldestTooLong`, `ClubPopularityRecoveryTooLong` |

모니터링 설정은 저장소가 사실상 소스다. 다만 Grafana 웹 UI에서 만든 대시보드·알림이 있다면
`grafana.db`(1.5MB)에만 있으므로 내보내야 한다.

## 9. 서버에만 있고 저장소에 없는 것

| 항목 | 이전 방법 |
|---|---|
| nginx 설정 (`dongarium.conf`, `monitor.conf`) | LB URL map |
| Let's Encrypt 인증서 2장 | Google 관리형 인증서 |
| `.env` (비밀값) | Secret Manager |
| `grafana.db` | 대시보드·알림 내보내기 |
| `schema_dump.sql` (2026-02) | 최신이 아니므로 새로 덤프 |

## 10. 발견한 문제

| 문제 | 내용 |
|---|---|
| **Swagger 외부 공개** | `/swagger-ui/index.html`과 `/v3/api-docs`(84KB, API 명세 전체)가 인증 없이 열려 있다 |
| `client_max_body_size` 미설정 | nginx 기본 1MB. 업로드 크기 제한 |
| 메모리 여유 없음 | 1528/1901MB, swap 없음 |
| 디스크 76% | 안 쓰는 Docker 이미지 30개, 5.4GB 회수 가능 |
| MySQL 컨테이너 로그 제한 없음 | 앱 컨테이너만 `max-size: 10m`이 적용되어 있다 |
| Redis 영속성 없음 | AOF 꺼짐. 재시작하면 로그아웃 토큰 블랙리스트와 refresh token이 사라진다 |
| MySQL 3306 외부 노출 | 보안 그룹에만 의존 |
| 배포 중 중단 | `docker compose down && up` 방식 |
| 이미지 태그 `latest` 단일 | 롤백이 어렵다 |

`/actuator/**`는 외부에서 접근되지 않는다. nginx에 규칙이 없어 `location /`로 빠지고,
CloudFront의 SPA 폴백 때문에 `index.html`이 200으로 돌아온다(상태 코드만 보면 열린 것처럼 보인다).

## 11. GCP 대응 관계

| 현재 | GCP |
|---|---|
| EC2 + docker compose | Cloud Run |
| nginx (TLS, 라우팅, 리다이렉트) | Global External HTTPS LB + Google 관리형 인증서 |
| CloudFront + S3 (프론트) | GCS 버킷 + Cloud CDN (LB 백엔드 버킷) |
| S3 (이미지, 첨부) | GCS |
| MySQL 컨테이너 | Cloud SQL for MySQL 8.0 |
| Redis 컨테이너 | Memorystore 또는 소형 VM |
| Prometheus / Grafana / Loki / promtail | Cloud Monitoring·Logging (+ 기존 provisioning 재사용) |
| Route 53 | Cloud DNS |
| ECR | Artifact Registry |
| `.env`, `APPLICATION_YML` secret | Secret Manager |

## 12. 보안 현황 (전환 설계 입력값)

현행 구성을 기록한 것이다. 조치 여부는 이 문서의 범위가 아니고, **GCP 쪽 설계가 충족해야 할 요건**을 뽑기 위한 자료다.

### 네트워크 접근

| 대상 | 현재 | 외부 접근 |
|---|---|---|
| 22 (SSH) | `0.0.0.0` | 보안 그룹 허용 (키 인증만, 등록 키 1개, `passwordauthentication no`) |
| 80, 443 (nginx) | `0.0.0.0` | 공개 |
| **8080 (앱)** | `0.0.0.0` | **공개.** `http://<IP>:8080/actuator/health` → 200. nginx를 우회해 앱에 직접 접근 가능 |
| 3306 (MySQL) | `0.0.0.0` 리스닝 | 외부에서 연결되지 않음 (보안 그룹이 차단) |
| 3000, 3100, 9090 (Grafana, Loki, Prometheus) | `127.0.0.1` | 직접 접근 불가. Grafana만 nginx를 통해 공개 |

- 호스트 방화벽은 없다(`firewalld` 비활성, iptables `INPUT ACCEPT`). 인바운드 통제는 보안 그룹에만 의존한다.
- fail2ban 같은 침입 차단 도구는 없다.

### 인증·노출

| 항목 | 현재 |
|---|---|
| Grafana 계정 | `.env`에 `GRAFANA_*` 키가 없어 compose 기본값(`admin` / `admin`)이 적용된다. `monitor.dongarium.co.kr`은 외부 공개 |
| Swagger | `/swagger-ui/index.html`, `/v3/api-docs`(84KB)가 인증 없이 조회된다 |
| `/actuator/**` | 도메인 경유로는 노출되지 않는다(SPA 폴백으로 `index.html` 반환). 단 `:8080` 직접 접근 경로로는 노출된다 |

### 아웃바운드

| 포트 | 상태 |
|---|---|
| 443 | 허용 |
| 587 (SMTP) | 허용 → Gmail SMTP 발송 경로 |
| 25 (SMTP) | 차단 (AWS 기본). GCP도 25번은 차단하므로 587을 쓰는 현재 방식이 그대로 동작한다 |

### 전환 시 현행과 동일하게 맞출 항목

현행 접근 범위를 그대로 재현하는 것이 기준이다. 더 엄격하게 바꾸지 않는다.

| 현행 | GCP에서 동일하게 맞추는 방법 |
|---|---|
| `/api/**` 공개 | LB URL map: `/api/*` → Cloud Run |
| `/swagger-ui/**`, `/v3/api-docs` 공개 | LB URL map에서 Cloud Run으로 전달 (차단하지 않는다) |
| `/actuator/**`는 도메인 경유 시 프론트 `index.html`이 반환됨 | `/actuator` 전용 규칙을 두지 않고 `/*`(프론트 버킷)로 보내면 동일하게 동작한다 |
| `monitor.dongarium.co.kr`에서 Grafana 공개, 계정은 compose 기본값 | 같은 호스트 규칙으로 Grafana를 공개하고 동일한 계정 환경변수 구성을 사용한다 |
| 앱 포트(8080)가 외부에서 직접 접근 가능 | Cloud Run은 LB를 통하지 않는 직접 접근 경로를 만들 수 없으므로 이 부분은 동일하게 재현되지 않는다 (LB 경유만 가능) |
| MySQL이 외부에서 연결되지 않음 | Cloud SQL private IP, 공개 IP 미생성 |
| Prometheus·Loki는 외부 비공개 | 외부 엔드포인트를 만들지 않는다 |
| 비밀값을 `.env`와 `APPLICATION_YML`로 주입 | Secret Manager에 저장하고 Cloud Run 환경변수로 주입 (전달 방식만 바뀌고 범위는 동일) |
| 아웃바운드 443·587 허용, 25 차단 | GCP 기본과 동일. Gmail SMTP(587) 그대로 동작 |
