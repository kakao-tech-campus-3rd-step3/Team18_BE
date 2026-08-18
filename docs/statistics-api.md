# 지원자 통계 API (프론트엔드 가이드)

지원폼(`ClubApplyForm`) 단위로 지원자의 **성별·입학연도(학번)·학부·일자별 지원 추이**를 집계해 제공합니다.
백엔드는 집계 결과(버킷과 타입)만 내려주고, 차트 표현 방식은 프론트가 정합니다.

엔드포인트는 두 개입니다.

| 용도 | 경로 | 인증 | 마스킹 | 캐시 |
|---|---|---|---|---|
| **공개** | `GET /api/club-apply-forms/{clubApplyFormId}/statistics` | 불필요(비로그인 허용) | 적용 | Redis TTL 30분 |
| **관리자** | `GET /api/club-apply-forms/{clubApplyFormId}/statistics/admin` | 해당 동아리 CLUB_ADMIN/CLUB_EXECUTIVE | **미적용(원본)** | 미사용(실시간) |

응답 스키마는 두 엔드포인트가 동일하며, 값만 다릅니다(관리자는 마스킹 없는 원본·실시간).

---

## 1. 요청

### 경로 파라미터
- `clubApplyFormId` (number, 필수) — 지원폼 ID.

### 쿼리 파라미터
- `dimensions` (string, 선택) — 조회할 집계 항목을 콤마로 나열. **생략하면 전체**를 반환.
  - 값: `GENDER`, `ADMISSION_YEAR`, `FACULTY`, `DAILY_APPLICATIONS`
  - 예: `?dimensions=GENDER,FACULTY`
  - 정의되지 않은 값이 하나라도 있으면 **400** (아래 에러 참고).

### 예시
```
GET /api/club-apply-forms/12/statistics
GET /api/club-apply-forms/12/statistics?dimensions=GENDER,ADMISSION_YEAR
GET /api/club-apply-forms/12/statistics/admin        (Authorization: Bearer <JWT>)
```

---

## 2. 응답 스키마

```jsonc
{
  "clubApplyFormId": 12,
  "totalApplicants": 214,        // 누적 지원자 수 (분포 아님 → 마스킹돼도 노출)
  "snapshot": false,             // 확정 스냅샷 여부. 현재 항상 false
  "masked": false,               // true면 재식별 방지로 분포 비공개(results=[])
  "calculatedAt": "2026-03-14T23:59:30+09:00",  // 이 통계가 '산출된 시각'(KST)
  "results": [
    {
      "dimension": "GENDER",     // 집계 항목
      "type": "CATEGORICAL",     // CATEGORICAL | ORDINAL | TIME_SERIES
      "buckets": [
        { "key": "MALE", "label": "남성", "count": 121, "ratio": 0.565 }
      ]
    }
  ]
}
```

### 필드
- **`totalApplicants`** — 누적 지원자 수. 분포가 아니므로 `masked=true`여도 그대로 내려갑니다.
- **`snapshot`** — 모집 종료 후 확정 스냅샷이면 true. **현재는 스냅샷 기능 미도입이라 항상 `false`.**
- **`masked`** — 재식별 방지 최소 공개 기준(전체 지원자 수 < 3) 미달로 분포를 비공개했으면 `true`. 이때 `results`는 빈 배열.
- **`calculatedAt`** — 이 통계 스냅샷이 **계산된 시각(KST)**. 공개 통계는 캐시되므로 조회 시각이 아니라 **캐시된 스냅샷의 계산 시각**이며, 최대 캐시 TTL(30분)만큼 과거일 수 있습니다. → 프론트는 이 값으로 **"HH:MM 기준"** 같은 신선도를 표시하세요.
- **`results[].dimension`** — 집계 항목 코드(위 4종).
- **`results[].type`** — 데이터 성격. 표현 방식 힌트:
  - `CATEGORICAL` — 범주(성별·학부). 파이/도넛 등.
  - `ORDINAL` — 순서 있는 범주(입학연도). 정렬된 막대 등.
  - `TIME_SERIES` — 시계열(일자별). 라인/영역 등. **`ratio` 없음.**
- **`results[].buckets[]`**
  - `key` — 표기 변경에 영향받지 않는 **코드값**(비교·매핑용).
  - `label` — **화면 표시용 문자열**.
  - `count` — 해당 버킷 인원. **`0`이면 실제 0명**(비공개와 다름, 아래 3단원 참고).
  - `ratio` — 전체 대비 비율(소수점 3자리 고정, 예 `0.565`). **`TIME_SERIES`에는 없음.**

---

## 3. "비공개 / 실제 0명 / 미입력" 3가지 상태 구분 ⚠️

프론트에서 반드시 구분해야 하는 세 가지입니다.

| 의미 | 표현 | 화면 예시 |
|---|---|---|
| **소수라 비공개** | `masked: true` + `results: []` | "지원자가 적어 통계 비공개" |
| **실제 0명** | 버킷 `count: 0` | 그 값 그대로 0으로 표시 (예: 그날 지원 0건) |
| **값 미기입** | `key:"UNKNOWN"`, `label:"미입력"` 버킷 | "미입력"으로 표시 |

- `masked=true`면 `results`가 통째로 비어 있으니, 분포 차트 대신 비공개 안내를 그리세요. `totalApplicants`는 여전히 유효합니다.
- `masked=false`인데 특정 버킷의 `count`가 0이면 그건 **진짜 0명**입니다.

---

## 4. dimension별 버킷 규격

### GENDER (`CATEGORICAL`)
| key | label |
|---|---|
| `MALE` | 남성 |
| `FEMALE` | 여성 |
| `UNKNOWN` | 미입력 (성별 미수집/미입력) |

- `UNKNOWN`은 항상 마지막.

### ADMISSION_YEAR (`ORDINAL`) — 입학연도(학번 앞 2자리)
| key | label | 비고 |
|---|---|---|
| `OLDER` | 그 이전 | 기준연도-6보다 오래된 학번을 묶음 |
| `22` | 22학번 | 최근 구간은 2자리 연도 코드 |
| `UNKNOWN` | 미입력 | 6자리 숫자가 아닌 학번 |

- **`key`는 2자리(`"22"`)** 코드입니다(4자리 아님). `label`이 표시용(`"22학번"`).
- 버킷 순서: **그 이전 → 개별 연도 오름차순 → 미입력**. (프론트가 재정렬하지 않아도 이 순서 보장)
- 6자리 학번 전체는 응답 어디에도 포함되지 않습니다.

### FACULTY (`CATEGORICAL`) — 학부
- `key`/`label`은 `Faculty` enum(예: `ENGINEERING` "공과대학", `NATURAL_SCIENCES` "자연과학대학" …).
- 목록 밖/기타는 `ETC` "기타", 미수집은 `UNKNOWN` "미입력".

### DAILY_APPLICATIONS (`TIME_SERIES`) — 일자별 지원 추이
| key | label | count | ratio |
|---|---|---|---|
| `2026-03-02` (ISO 날짜) | `3월 2일` | 그날 접수 수 | (없음) |

- 모집 시작일~`min(모집마감, 오늘)` 사이 **지원자 없는 날도 `count:0`으로 채워** 내려갑니다. 마감 이후 미래 날짜는 채우지 않습니다.
- 날짜 오름차순 정렬 보장. `ratio` 없음.
- 표시 구간(예: 최근 N일)은 프론트가 잘라서 결정하세요.

---

## 5. 공개 vs 관리자 차이

- **공개(`/statistics`)**: 비로그인 조회 가능. 전체 지원자 < 3이면 `masked=true`. **30분 캐시**(→ `calculatedAt`으로 신선도 표시).
- **관리자(`/statistics/admin`)**: 해당 동아리 관리자만. **마스킹 없음**(항상 원본), **캐시 없음**(실시간). 따라서 `masked`는 항상 `false`, `calculatedAt`은 조회 시각에 가깝습니다.

---

## 6. 에러 응답

에러는 공통 형식입니다.
```json
{ "error_code": "FORM_NOT_FOUND", "message": "지원폼이 존재하지 않습니다", "detail": "clubApplyFormId = 999" }
```

| 상황 | HTTP | `error_code` |
|---|---|---|
| 잘못된 id 타입(`/abc/statistics`) | 400 | `INVALID_INPUT_VALUE` |
| 지원하지 않는 dimension | 400 | `UNSUPPORTED_STATISTICS_DIMENSION` |
| 없는 지원폼 | 404 | `FORM_NOT_FOUND` |
| (관리자) 미인증 | 401 | `UNAUTHENTICATED_USER` |
| (관리자) 해당 동아리 관리자 아님 | 403 | `FORBIDDEN` |
| 서버 오류 | 500 | `INTERNAL_SERVER_ERROR` |

- Redis 캐시 장애는 에러가 아니라 실시간 계산으로 자동 폴백하므로, 클라이언트에는 정상 200으로 응답합니다.

---

## 7. 응답 예시

### 정상 (공개, 진행 중)
```json
{
  "clubApplyFormId": 12,
  "totalApplicants": 214,
  "snapshot": false,
  "masked": false,
  "calculatedAt": "2026-03-14T23:59:30+09:00",
  "results": [
    {
      "dimension": "GENDER",
      "type": "CATEGORICAL",
      "buckets": [
        { "key": "MALE", "label": "남성", "count": 121, "ratio": 0.565 },
        { "key": "FEMALE", "label": "여성", "count": 91, "ratio": 0.425 },
        { "key": "UNKNOWN", "label": "미입력", "count": 2, "ratio": 0.010 }
      ]
    },
    {
      "dimension": "ADMISSION_YEAR",
      "type": "ORDINAL",
      "buckets": [
        { "key": "22", "label": "22학번", "count": 58, "ratio": 0.271 }
      ]
    },
    {
      "dimension": "FACULTY",
      "type": "CATEGORICAL",
      "buckets": [
        { "key": "ENGINEERING", "label": "공과대학", "count": 74, "ratio": 0.346 },
        { "key": "NATURAL_SCIENCES", "label": "자연과학대학", "count": 37, "ratio": 0.173 },
        { "key": "ETC", "label": "기타", "count": 20, "ratio": 0.093 }
      ]
    },
    {
      "dimension": "DAILY_APPLICATIONS",
      "type": "TIME_SERIES",
      "buckets": [
        { "key": "2026-03-02", "label": "3월 2일", "count": 12 }
      ]
    }
  ]
}
```

### 최소 공개 기준 미달(비공개)
```json
{
  "clubApplyFormId": 12,
  "totalApplicants": 2,
  "snapshot": false,
  "masked": true,
  "calculatedAt": "2026-03-14T23:59:30+09:00",
  "results": []
}
```
