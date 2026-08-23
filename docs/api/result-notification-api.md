# 결과 발표 및 알림 API 명세

## 변경 요약

- 결과 알림 채널로 `EMAIL`, `SMS`를 지원합니다.
- `channels`에서 여러 채널을 동시에 선택할 수 있습니다.
- `channels`를 생략하거나 `null`로 전달하면 `EMAIL`이 기본 적용됩니다.
- `channels: []`는 기본값이 적용되지 않고 `400 Bad Request`가 반환됩니다.
- 결과 발표 요청에는 `Idempotency-Key` 헤더가 필수입니다.
- 최근 결과 발표 요청의 채널별 발송 현황을 조회하는 API가 추가되었습니다.

---

# 1. 합격/불합격 처리 및 결과 알림 요청

```http
PATCH /api/clubs/{clubId}/club-apply-form/result?stage={stage}
```

동아리 운영진이 지원자의 합격/불합격 결과를 처리하고 선택한 채널로 결과 알림 발송을 요청합니다.

실제 이메일 및 SMS 발송은 별도 발송 작업으로 처리됩니다. 정상 응답의 `success: true`는 지원 결과 처리와 발송 작업 생성이 완료되었다는 의미이며, 모든 수신자에게 실제 전달이 완료되었다는 의미는 아닙니다.

## 권한

- 로그인 필수
- 해당 동아리의 관리자 또는 운영진만 호출할 수 있습니다.

# 요청

## Path Parameter

```yaml
필수 여부: 필수
파라미터 명: clubId
파라미터 종류: Long
설명: 결과를 발표할 동아리 ID
```

## Query Parameter

```yaml
필수 여부: 필수
파라미터 명: stage
파라미터 종류: INTERVIEW, FINAL
설명: 발표할 전형 단계
```

- `INTERVIEW`: 면접 결과 발표
- `FINAL`: 최종 결과 발표
- `RESULT` 및 그 외 값은 사용할 수 없습니다.

## Header

```yaml
필수 여부: 필수
헤더 명: Idempotency-Key
값 형식: 1자 이상 100자 이하 문자열
권장 형식: UUID
예시: 550e8400-e29b-41d4-a716-446655440000
```

프론트엔드는 최초 결과 발표 시 키를 한 번 생성하고, 같은 요청을 네트워크 문제로 재시도할 때 반드시 동일한 키를 사용해야 합니다.

- 같은 키와 같은 요청: 기존 처리 결과 반환
- 같은 키와 다른 `stage`, `message`, `channels`: `409 Conflict`
- 새로운 결과 발표: 새로운 키 사용

서버는 `clubId`, `stage`, `message`, 정렬된 `channels`를 기준으로 동일 요청인지 비교합니다. 따라서 채널 배열의 순서만 다른 요청은 같은 요청으로 처리됩니다.

## Request Body

```json
{
  "message": "자세한 일정은 추후 안내드리겠습니다.",
  "channels": ["EMAIL", "SMS"]
}
```

### message

```yaml
필수 여부: 선택
파라미터 명: message
파라미터 종류: String 또는 null
최대 길이: 800자
설명: 합격자에게 추가로 전달할 안내 메시지
```

### channels

```yaml
필수 여부: 선택
파라미터 명: channels
파라미터 종류: EMAIL, SMS
다중 선택: 가능
기본값: EMAIL
```

| 요청 값 | 처리 결과 |
| --- | --- |
| 필드 생략 | `EMAIL` |
| `null` | `EMAIL` |
| `["EMAIL"]` | 이메일 발송 |
| `["SMS"]` | 문자 발송 |
| `["EMAIL", "SMS"]` | 이메일과 문자 모두 발송 |
| `[]` | `400 Bad Request` |
| `["PUSH"]` | `400 Bad Request` |

## 요청 예시

### 이메일과 문자 모두 발송

```http
PATCH /api/clubs/17/club-apply-form/result?stage=INTERVIEW
Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000
Content-Type: application/json
```

```json
{
  "message": "면접 결과와 다음 일정을 확인해주세요.",
  "channels": ["EMAIL", "SMS"]
}
```

### 기존 이메일 방식 사용

```json
{
  "message": "면접 결과를 안내드립니다."
}
```

`channels`를 생략했으므로 이메일만 발송합니다. 이 경우에도 `Idempotency-Key` 헤더는 필수입니다.

# 응답

## 정상

### `200 OK`

```json
{
  "success": true
}
```

`success: true`는 다음 작업이 완료되었다는 뜻입니다.

- 지원자 합격/불합격 결과 처리
- 선택한 수신자와 채널별 발송 작업 DB 저장
- 비동기 발송 요청 등록

최종 전달 결과는 결과 알림 발송 현황 조회 API로 확인합니다.

## 에러

### 채널이 빈 배열인 경우

`400 Bad Request`

```json
{
  "error_code": "INVALID_INPUT_VALUE",
  "message": "입력 값이 올바르지 않습니다.",
  "detail": "channels: 알림 채널은 하나 이상 선택해야 합니다."
}
```

### 지원하지 않는 채널인 경우

`400 Bad Request`

```json
{
  "error_code": "INVALID_INPUT_VALUE",
  "message": "입력 값이 올바르지 않습니다.",
  "detail": "요청 본문의 형식 또는 값이 올바르지 않습니다."
}
```

### Idempotency-Key 헤더가 없는 경우

`400 Bad Request`

```json
{
  "error_code": "INVALID_INPUT_VALUE",
  "message": "입력 값이 올바르지 않습니다.",
  "detail": "필수 헤더 'Idempotency-Key'가 요청에 포함되지 않았습니다."
}
```

### Idempotency-Key 길이가 잘못된 경우

`400 Bad Request`

```json
{
  "error_code": "INVALID_INPUT_VALUE",
  "message": "입력 값이 올바르지 않습니다.",
  "detail": "Idempotency-Key는 1자 이상 100자 이하여야 합니다."
}
```

### 동일한 키를 다른 요청에 재사용한 경우

`409 Conflict`

```json
{
  "error_code": "IDEMPOTENCY_KEY_CONFLICT",
  "message": "동일한 Idempotency-Key가 다른 결과 발표 요청에 사용되었습니다."
}
```

### 지원서 처리가 완료되지 않은 경우

`400 Bad Request`

```json
{
  "error_code": "PENDING_APPLICATION_EXIST",
  "message": "미처리 지원서가 존재합니다. 모든 지원서를 승인/거절로 확정한 뒤 발송하세요."
}
```

### 면접 합격자의 면접 일정이 없는 경우

`400 Bad Request`

```json
{
  "error_code": "UNSCHEDULED_ACCEPTED_APPLICANT_EXISTS",
  "message": "면접 시간을 결정하지 않은 합격자가 존재합니다. 모든 합격자의 면접 시간을 결정해주세요."
}
```

### 한 요청의 문자 수신자가 제한을 초과한 경우

`400 Bad Request`

```json
{
  "error_code": "NOTIFICATION_SMS_LIMIT_EXCEEDED",
  "message": "한 번에 발송할 수 있는 문자 알림 수를 초과했습니다.",
  "detail": "requestedCount=51, maxCount=50"
}
```

SMS가 선택된 경우에만 검사합니다. 기본 최대 인원은 50명이며 운영 설정에 따라 달라질 수 있습니다.

### 권한이 없는 경우

`403 Forbidden`

```json
{
  "error_code": "FORBIDDEN",
  "message": "해당 요청에 대한 권한이 없습니다."
}
```

### 일시적인 중복 처리 충돌이 발생한 경우

`409 Conflict`

```json
{
  "error_code": "TEMPORARY_SERVER_CONFLICT",
  "message": "일시적인 요청 충돌이 발생했습니다. 잠시 후 다시 시도해주세요."
}
```

이 경우 프론트엔드는 잠시 후 동일한 `Idempotency-Key`로 재시도해야 합니다.

---

# 2. 결과 알림 발송 현황 조회

```http
GET /api/clubs/{clubId}/result-notifications?limit={limit}
```

최근 결과 발표 요청별 발송 상태와 문자 유형별 예상비용을 조회합니다.

## 권한

- 로그인 필수
- 해당 동아리의 관리자 또는 운영진만 호출할 수 있습니다.

# 요청

## Path Parameter

```yaml
필수 여부: 필수
파라미터 명: clubId
파라미터 종류: Long
설명: 조회할 동아리 ID
```

## Query Parameter

```yaml
필수 여부: 선택
파라미터 명: limit
파라미터 종류: Integer
기본값: 20
최솟값: 1
최댓값: 100
설명: 최근 요청 조회 개수
```

# 응답

## 정상

### `200 OK`

```json
{
  "requests": [
    {
      "requestId": 10,
      "idempotencyKey": "550e8400-e29b-41d4-a716-446655440000",
      "stage": "FINAL",
      "requestStatus": "COMPLETED",
      "requestedAt": "2026-08-13T13:00:00",
      "total": 5,
      "pending": 1,
      "accepted": 1,
      "sent": 2,
      "failed": 1,
      "unknown": 0,
      "sms": 1,
      "lms": 1,
      "estimatedCost": 69.3
    }
  ]
}
```

### Response Body 필드

| 필드 | 설명 |
| --- | --- |
| `requestId` | 결과 발표 요청 ID |
| `idempotencyKey` | 요청에 사용한 중복 방지 키 |
| `stage` | `INTERVIEW` 또는 `FINAL` |
| `requestStatus` | 요청 처리 상태: `PROCESSING`, `COMPLETED` |
| `requestedAt` | 결과 발표 요청 시각 |
| `total` | 생성된 전체 채널별 발송 작업 수 |
| `pending` | 발송 대기 또는 발송 요청 처리 중인 작업 수 |
| `accepted` | SOLAPI가 접수했지만 최종 결과가 아직 없는 작업 수 |
| `sent` | 발송 성공이 확인된 작업 수 |
| `failed` | 통신사 실패 또는 재시도 종료가 확정된 작업 수 |
| `unknown` | 외부 접수 여부를 확인할 수 없는 작업 수 |
| `sms` | 실제 SMS 유형으로 분류된 작업 수 |
| `lms` | 실제 LMS 유형으로 분류된 작업 수 |
| `estimatedCost` | 설정 단가 기준 문자 예상비용 합계 |

`total`은 지원자 수가 아니라 채널별 작업 수입니다. 예를 들어 지원자 5명에게 `EMAIL`, `SMS`를 모두 선택하면 최대 10개의 발송 작업이 생성됩니다.

## 에러

### limit 범위가 잘못된 경우

`400 Bad Request`

```json
{
  "error_code": "INVALID_INPUT_VALUE",
  "message": "입력 값이 올바르지 않습니다.",
  "detail": "limit: 100 이하여야 합니다."
}
```

### 권한이 없는 경우

`403 Forbidden`

```json
{
  "error_code": "FORBIDDEN",
  "message": "해당 요청에 대한 권한이 없습니다."
}
```

---

# 프론트엔드 적용 체크리스트

- [ ] 결과 발표 UI에서 `EMAIL`, `SMS`를 복수 선택할 수 있도록 구현합니다.
- [ ] 최소 한 개 이상의 채널을 선택하도록 UI에서 검증합니다.
- [ ] 최초 요청 직전에 UUID 형식의 `Idempotency-Key`를 생성합니다.
- [ ] 네트워크 재시도에는 최초 요청과 동일한 키를 사용합니다.
- [ ] 사용자가 내용이나 채널을 변경해 새로 요청하면 새로운 키를 생성합니다.
- [ ] `200 OK`를 최종 수신 완료로 표시하지 않고 발송 요청 접수로 표시합니다.
- [ ] 필요하면 발송 현황 조회 API를 호출해 `sent`, `failed`, `unknown` 상태를 표시합니다.
- [ ] `409 TEMPORARY_SERVER_CONFLICT`는 동일한 키로 재시도합니다.
- [ ] `409 IDEMPOTENCY_KEY_CONFLICT`는 자동 재시도하지 않고 새로운 요청 여부를 확인합니다.
