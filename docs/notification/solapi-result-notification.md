# SOLAPI 결과 알림 개발·운영 가이드

## 1. 적용 범위

지원 결과 발표 API에서 `EMAIL`, `SMS`를 하나 이상 선택해 발송한다. 이메일은 SMTP 서버가 요청을 수락하면 `SENT`, 문자는 SOLAPI 접수 후 웹훅 또는 상태 조회에서 통신사 결과가 확인되어야 `SENT` 또는 `FAILED`가 된다.

현재 문자는 SOLAPI의 SMS/LMS를 사용한다. 카카오 알림톡은 발신 프로필과 템플릿 심사 등 별도 운영 조건이 필요해 이번 범위에서 제외했다.

## 2. API 계약

```http
PATCH /api/clubs/{clubId}/club-apply-form/result?stage=INTERVIEW
Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000
Content-Type: application/json
```

```json
{
  "message": "자세한 일정은 추후 안내드리겠습니다.",
  "channels": ["EMAIL", "SMS"]
}
```

- `stage`: `INTERVIEW` 또는 `FINAL`
- `channels`: `EMAIL`, `SMS` 중 하나 이상. 복수 선택 가능
- `channels`를 생략하거나 `null`로 보내면 하위 호환을 위해 `EMAIL`로 처리한다.
- 빈 배열은 `400 Bad Request`이며 이메일로 대체하지 않는다.
- `Idempotency-Key`: 1~100자의 필수 헤더. 프론트엔드는 최초 결과 발표 시 한 번 생성하고, 응답을 받지 못해 같은 요청을 재시도할 때도 반드시 같은 값을 사용한다.
- 같은 동아리에서 동일 키와 동일 요청을 다시 보내면 기존 결과를 반환한다. 같은 키로 `stage`, `message`, `channels`가 다른 요청을 보내면 `409 Conflict`를 반환한다.
- `200 OK`는 지원 결과 처리와 발송 작업 저장이 완료됐다는 뜻이다. 비동기 발송의 최종 성공을 의미하지 않는다.

SMS를 선택한 한 번의 결과 발표 요청은 기본 50명까지 허용한다. 이메일과 SMS를 함께 선택해도 SMS 수신자 수만 이 제한에 영향을 준다.

운영진은 다음 API에서 최근 요청별 발송 현황을 조회할 수 있다.

```http
GET /api/clubs/{clubId}/result-notifications?limit=20
```

해당 동아리 관리자 또는 운영진만 접근할 수 있으며 전체·대기·접수·성공·실패·미확정 건수와 SMS/LMS 건수, 설정 단가 기준 예상 비용 합계를 반환한다. 수신자 주소와 본문은 반환하지 않는다.

SOLAPI 발송의 `customFields`에는 내부 `notificationDeliveryId`와 결과 발표 `idempotencyKey`를 전달한다. 웹훅 또는 SOLAPI 콘솔의 메시지 정보와 내부 작업을 연결할 때 사용하며, 수신번호나 본문은 customFields에 넣지 않는다.

## 3. 발송 상태

| 상태 | 의미 | 자동 처리 |
|---|---|---|
| `PENDING` | 최초 발송 또는 재시도 시각을 기다림 | 발송 스케줄러가 처리 |
| `SENDING` | 한 서버가 작업을 선점해 외부 요청 중 | 10분 이상 정체 시 `UNKNOWN` |
| `ACCEPTED` | SOLAPI가 문자를 접수했지만 최종 결과 미확정 | 상태 조회 스케줄러가 확인 |
| `SENT` | 이메일 서버 수락 또는 문자 최종 성공 | 종료 |
| `FAILED` | SOLAPI 접수 후 통신사 실패 확인 | 종료 및 개발자 알림 |
| `UNKNOWN` | 외부 접수 여부나 최종 결과를 안전하게 확정할 수 없음 | 자동 재발송 금지, 개발자 확인 |
| `PERMANENTLY_FAILED` | 입력·인증 오류 또는 재시도 소진 | 종료 및 개발자 알림 |

`UNKNOWN`은 중복 문자 위험이 있으므로 자동으로 `PENDING`으로 돌리지 않는다.

## 4. 개발·배포 전 체크리스트

### SOLAPI 계정

- [ ] SOLAPI API Key와 API Secret을 발급했다.
- [ ] 실제 사용할 발신번호 등록과 본인 인증이 완료됐다.
- [ ] API 키에 필요한 최소 권한만 부여했다.
- [ ] 키와 Secret을 저장소, 로그, 이슈에 기록하지 않고 배포 환경의 Secret으로 관리한다.
- [ ] 테스트용 수신번호와 테스트 발송에 대한 팀 동의를 받았다.
- [ ] 개인정보 처리방침과 문자 수신 동의 범위가 지원 결과 안내를 포함하는지 확인했다.

### 과금·호출 제한

- [ ] 팀이 감당할 수 있는 시간당 최대 호출 수를 정했다.
- [ ] 시간·일·월 호출 한도와 월 예상비용 상한을 운영값으로 명시했다.
- [ ] `NOTIFICATION_SMS_ESTIMATED_COST_SMS`, `NOTIFICATION_SMS_ESTIMATED_COST_LMS`를 현재 계약 단가로 설정했다.
- [ ] 한글 본문이 90바이트를 넘으면 LMS로 발송되어 단가가 달라질 수 있음을 확인했다.
- [ ] 예상 최대 비용을 `시간당 호출 한도 × LMS 단가` 기준으로 계산했다.
- [ ] 애플리케이션 잔액 경고 기준과 SOLAPI 콘솔의 잔액 부족 알림을 함께 설정했다.

애플리케이션은 요청당 SMS 수, 공유 DB 기준 시간·일·월 호출 수, 설정 단가 기준 월 예상비용을 제한한다. 70%·90%·100% 도달은 기간별 한 번만 개발자에게 알린다. 선택적으로 SDK 잔액 조회를 활성화해 잔액+포인트가 기준 미만이면 경고할 수 있다. 실제 청구액과 잔액은 SOLAPI 정책이 기준이므로 콘솔 알림도 함께 설정한다.

### 메시지

- [ ] 합격·불합격, 면접·최종 결과별 문구를 검토했다.
- [ ] 발신자와 동아리명을 본문에서 식별할 수 있다.
- [ ] 본문이 통신사 기준 2,000바이트 이하인지 확인했다.
- [ ] 지원자의 전화번호가 대한민국 휴대전화 형식인지 확인했다.
- [ ] 민감정보, 인증정보, 내부 오류 내용을 본문에 넣지 않았다.

## 5. 환경변수

### 필수

| 환경변수 | 설명 |
|---|---|
| `SOLAPI_ENABLED` | 운영 준비가 끝난 뒤에만 `true` |
| `SOLAPI_API_KEY` | SOLAPI API Key |
| `SOLAPI_API_SECRET` | SOLAPI API Secret |
| `SOLAPI_SENDER_NUMBER` | SOLAPI에 등록된 발신번호. 숫자만 권장 |
| `SOLAPI_WEBHOOK_SECRET` | SOLAPI 웹훅 등록 Secret 원문 |

`SOLAPI_ENABLED=false`이면 SMS 발송기와 웹훅 엔드포인트는 등록되지 않는다. 활성화할 때 키, API Secret, 발신번호, Webhook Secret이 하나라도 비어 있으면 애플리케이션 시작에 실패한다. 이메일을 포함한 DB 발송 작업 처리는 `NOTIFICATION_DISPATCH_ENABLED`로 별도 제어한다.

### 한도·스케줄러

| 환경변수 | 기본값 | 설명 |
|---|---:|---|
| `NOTIFICATION_RESULT_MAX_SMS_PER_REQUEST` | 50 | 결과 발표 한 건의 최대 SMS 수신자 수 |
| `NOTIFICATION_RESULT_MAX_SOLAPI_CALLS_PER_HOUR` | 50 | 모든 서버 인스턴스가 공유하는 시간당 호출 한도 |
| `NOTIFICATION_RESULT_MAX_SOLAPI_CALLS_PER_DAY` | 50 | 일일 호출 한도 |
| `NOTIFICATION_RESULT_MAX_SOLAPI_CALLS_PER_MONTH` | 1500 | 월간 호출 한도 |
| `NOTIFICATION_RESULT_MAX_SOLAPI_ESTIMATED_COST_PER_MONTH` | 0 | 월 예상비용 상한. 0이면 비용 상한만 비활성화 |
| `NOTIFICATION_SMS_ESTIMATED_COST_SMS` | 0 | SMS 한 건 예상 단가. 운영에서 반드시 설정 |
| `NOTIFICATION_SMS_ESTIMATED_COST_LMS` | 0 | LMS 한 건 예상 단가. 운영에서 반드시 설정 |
| `NOTIFICATION_DISPATCH_ENABLED` | false | 커밋 후 즉시 발송과 DB 복구 스케줄러 활성화 |
| `NOTIFICATION_DISPATCH_SCHEDULER_DELAY_MS` | 30000 | 발송 작업 조회 주기 |
| `NOTIFICATION_DISPATCH_BATCH_SIZE` | 50 | 한 번에 조회하는 작업 수 |
| `NOTIFICATION_DISPATCH_MAX_ATTEMPTS` | 5 | 일시 오류 최대 시도 수. 호출 한도 대기는 제외 |
| `NOTIFICATION_DISPATCH_INITIAL_RETRY_DELAY_SECONDS` | 60 | 최초 재시도 대기 |
| `NOTIFICATION_DISPATCH_MAX_RETRY_DELAY_SECONDS` | 3600 | 지수 백오프 최대 대기 |
| `NOTIFICATION_DISPATCH_SENDING_TIMEOUT_SECONDS` | 600 | 정체된 `SENDING`을 `UNKNOWN`으로 바꾸는 기준 |
| `NOTIFICATION_STATUS_SCHEDULER_DELAY_MS` | 60000 | SOLAPI 최종 상태 동기화 주기 |
| `NOTIFICATION_STATUS_BATCH_SIZE` | 50 | 상태 조회 배치 크기 |
| `NOTIFICATION_STATUS_CHECK_INTERVAL_SECONDS` | 60 | 미확정 건의 다음 조회 간격 |
| `NOTIFICATION_STATUS_MAX_ACCEPTED_AGE_HOURS` | 24 | `ACCEPTED` 상태 최대 확인 시간 |
| `NOTIFICATION_MONITORING_ENABLED` | false | 상태 메트릭·개발자 실패 알림 활성화 |
| `NOTIFICATION_MONITORING_SCHEDULER_DELAY_MS` | 300000 | 모니터링 주기 |
| `NOTIFICATION_MONITORING_FAILURE_ALERT_BATCH_SIZE` | 100 | 한 번에 보고할 신규 최종 실패 수 |
| `NOTIFICATION_MONITORING_LONG_PENDING_AGE_MINUTES` | 60 | 장기 PENDING 경고 기준 |
| `SOLAPI_RECIPIENT_ALLOWLIST_ENABLED` | true | 허용 번호 외 실제 문자 발송 차단 |
| `SOLAPI_RECIPIENT_ALLOWLIST` | 빈 값 | 쉼표로 구분한 테스트 수신번호 목록 |
| `NOTIFICATION_RETENTION_ENABLED` | false | 종료 알림의 민감정보 보관기간 비식별화 활성화 |
| `NOTIFICATION_RETENTION_DAYS` | 90 | 민감정보 보관 일수. 팀 합의값으로 변경 |
| `NOTIFICATION_RETENTION_BATCH_SIZE` | 100 | 한 번에 비식별화할 발송 작업 수 |
| `NOTIFICATION_RETENTION_SCHEDULER_DELAY_MS` | 86400000 | 비식별화 스케줄러 실행 간격 |
| `NOTIFICATION_SOLAPI_BALANCE_ENABLED` | false | SOLAPI 잔액 부족 주기 조회 활성화 |
| `NOTIFICATION_SOLAPI_BALANCE_LOW_THRESHOLD` | 5000 | 잔액+포인트 부족 경고 기준 |
| `NOTIFICATION_SOLAPI_BALANCE_SCHEDULER_DELAY_MS` | 3600000 | SOLAPI 잔액 조회 주기 |

허용 목록 보호가 활성화된 상태에서 목록이 비어 있으면 모든 문자 발송이 차단된다. 개발·스테이징에서는 테스트 번호를 넣고, 전체 운영 발송은 팀 합의 후 `SOLAPI_RECIPIENT_ALLOWLIST_ENABLED=false`로 명시한다.

### 웹훅

- URL: `POST https://{서비스 도메인}/api/webhooks/solapi`
- 이벤트: `SINGLE-REPORT`
- 등록 Secret: `SOLAPI_WEBHOOK_SECRET`과 같은 원문
- 서버는 `X-Solapi-Secret`의 SHA-1 해시를 검증한다.
- 정상 요청은 비동기 처리를 예약하고 즉시 200을 반환한다.
- 웹훅이 누락되어도 상태 조회 스케줄러가 `ACCEPTED` 작업을 보완한다.

## 6. 운영 DB 적용 순서

애플리케이션 배포 전에 다음 SQL을 순서대로 MySQL 8.x에 적용한다.

1. `docs/database/migrations/20260803_create_notification_delivery.sql`
2. `docs/database/migrations/20260803_add_notification_idempotency.sql`
3. `docs/database/migrations/20260812_add_notification_delivery_reply_to.sql`
4. `docs/database/migrations/20260812_create_notification_quota_bucket.sql`
5. `docs/database/migrations/20260812_add_notification_failure_alert.sql`
6. `docs/database/migrations/20260813_expand_notification_quota_periods.sql`
7. `docs/database/migrations/20260813_add_notification_pending_alert.sql`
8. `docs/database/migrations/20260817_add_notification_redaction.sql`
9. `docs/database/migrations/20260818_add_notification_message_cost.sql`

운영 적용 전 스냅샷이나 백업을 확보하고 스테이징과 같은 MySQL 버전에서 먼저 실행한다.

## 7. 권장 배포 순서

1. DB 마이그레이션을 적용한다.
2. `SOLAPI_ENABLED=false`, `NOTIFICATION_DISPATCH_ENABLED=false`로 배포해 DB 작업 생성만 확인한다.
3. API Key/Secret, Webhook Secret, 등록 발신번호와 낮은 시간·일·월 한도를 설정한다.
4. 허용 목록에 개발자 번호 한 건만 등록하고 `SOLAPI_ENABLED=true`, `NOTIFICATION_DISPATCH_ENABLED=true`로 단건 발송을 확인한다.
5. DB에서 `PENDING → SENDING → ACCEPTED → SENT`와 `provider_message_id` 저장을 확인한다.
6. 웹훅 `4000` 수신과 `notification.delivery.status` 메트릭, 운영 Discord 알림 경로를 확인한다.
7. 이메일+SMS 복수 채널을 확인한 뒤 한도와 허용 목록을 운영값으로 전환한다.

테스트 발송에서도 프론트엔드는 네트워크 재시도에 동일한 `Idempotency-Key`를 사용해야 한다.

## 8. 장애 대응

- `PENDING` 증가: `SOLAPI_ENABLED`, 발송 스케줄러, 시간당 한도와 다음 시도 시각을 확인한다.
- `ACCEPTED` 증가: SOLAPI 상태 조회 가능 여부와 API 인증, 상태 스케줄러를 확인한다.
- `PERMANENTLY_FAILED`: 전화번호 형식, 인증·권한, 발신번호, 재시도 소진 여부를 확인한다.
- `FAILED`: SOLAPI 콘솔에서 공급자 메시지 ID의 통신사 실패 사유를 확인한다.
- `UNKNOWN`: SOLAPI 콘솔에서 실제 발송 여부를 수동 확인한다. 확인 전 동일 수신자에게 자동 또는 수동 재발송하지 않는다.
- 시간·일·월 한도 도달: 작업은 해당 기간의 다음 버킷으로 연기되며 최대 재시도 횟수를 소모하지 않는다.

운영 `prod` 프로필은 최종 실패 집계 `ERROR` 로그를 기존 Discord Appender로 전달한다. 로그에는 발송 작업 ID, 채널, 상태, 오류 코드만 포함하며 수신번호와 메시지 본문은 포함하지 않는다. `failure_alerted_at`이 기록되므로 같은 실패는 한 번만 알린다.

잔액 모니터링을 활성화하면 부족 상태 진입 시 한 번만 `ERROR`로 알리고, 기준 이상으로 회복된 뒤 다시 부족해질 때 재알림한다. 조회 오류도 정상 조회가 회복되기 전까지 한 번만 알린다. 여러 서버 인스턴스에서는 인스턴스별 한 번씩 알림이 발생할 수 있으므로 Discord 알림 수를 고려한다.

긴급 중단 시 `SOLAPI_ENABLED=false`로 재배포하면 신규 SOLAPI 호출을 막을 수 있다. 모든 비동기 발송도 멈춰야 한다면 `NOTIFICATION_DISPATCH_ENABLED=false`를 함께 사용한다. 이미 SOLAPI가 접수한 문자는 설정 변경으로 취소되지 않는다.

## 9. 운영 전 반드시 확정할 외부 항목

- [ ] 개인 계정의 실제 승인 일일 한도와 월 예산을 확인했다.
- [ ] 발신번호 노출 및 회신·전화 문의 담당자를 확정했다.
- [ ] 전화번호·메시지 본문·발송 결과의 법적/운영 보관 기간을 확정했다.
- [ ] SOLAPI 콘솔에 운영 웹훅을 등록하고 테스트 전송을 확인했다.
- [ ] 실제 개발자 번호 단건 발송으로 groupId/messageId/웹훅을 확인했다.

보관 기간이 확정되기 전에는 `NOTIFICATION_RETENTION_ENABLED=false`를 유지한다. 합의 후 `NOTIFICATION_RETENTION_DAYS`를 설정하고 스테이징에서 대상 건수를 확인한 뒤 활성화한다.

비식별화는 `SENT`, `FAILED`, `UNKNOWN`, `PERMANENTLY_FAILED` 상태 중 보관기간을 지난 작업에만 적용한다. 수신 전화번호/이메일, 회신 주소, 제목, 본문, 마지막 오류 메시지를 제거하며 다음 감사·장애 대응 정보는 유지한다.

- 내부 발송 ID, 동아리/사용자/지원서 ID
- 채널, 결과 유형, 최종 상태와 처리 시각
- SOLAPI `groupId`, `messageId`, 상태·오류 코드
- SMS/LMS 유형과 발송 당시 설정 단가 기준 예상 비용
- 시도 횟수와 생성·수정 시각

진행 중인 `PENDING`, `SENDING`, `ACCEPTED` 작업은 비식별화하지 않으며, 한 번 처리된 작업은 `redacted_at`으로 중복 처리를 막는다.

## 10. 참고자료

- [SOLAPI SDK 시작하기](https://solapi.com/developers/sdk/start)
- [SOLAPI Java 발송 예제](https://solapi.com/developers/sdk/java-sendingexample)
- [SOLAPI 공식 Kotlin/Java SDK](https://github.com/solapi/solapi-kotlin)
- [SOLAPI 웹훅 API](https://solapi.com/developers/api/webhook)
