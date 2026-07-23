# 지원자 통계 설정 (#335)

`src/main/resources/application.yml`은 **`.gitignore` 대상**(`.gitignore:226`)이며 GitHub Secret
`APPLICATION_YML`에서 주입된다. 따라서 아래 설정은 리포지토리에 커밋되지 않으므로, **배포 전에 Secret을 직접
갱신해야 한다.**

모든 항목은 `StatisticsProperties`에 `@DefaultValue`로 기본값이 있어, 설정을 추가하지 않아도 애플리케이션은
기본값으로 정상 동작한다. 값을 바꾸고 싶을 때만 추가하면 된다.

## 추가할 내용

기본 프로필(문서 최상단, 프로필 구분자 `---` 이전)에 root 레벨로 추가한다.

```yaml
statistics:
  masking:
    # 이 값보다 적은 지원자를 가진 버킷은 '기타'로 합친다. 소수 집단 재식별 방지 관례값이 5.
    bucket-threshold: 5
    # 누적 지원자가 이 값보다 적으면 속성 분포를 아예 공개하지 않는다.
    min-public-total: 10
  department:
    # 학과는 값 종류가 많으므로 상위 N개만 노출한다.
    top-n: 5
    # 응답에 포함되는 학과 문자열의 최대 길이.
    max-label-length: 30
  admission-year:
    # 유효한 입학연도의 하한. 이보다 이르면 '미입력'으로 분류한다.
    min-year: 1990
  precompute:
    enabled: true
    # 집계 주기. 통계 신선도는 이 값이 결정한다.
    cron: "0 */10 * * * *"
    # 직전 공개 대비 지원자가 이만큼 늘어야 캐시를 갱신한다.
    publish-step: 1
    cache-ttl: PT2H
    lock-ttl: PT5M
```

`test` 프로필 문서에는 아래를 root 레벨로 추가한다. 테스트에서 Redis와 스케줄러에 의존하지 않게 한다.

```yaml
statistics:
  precompute:
    enabled: false
```

## 운영 시 알아둘 점

- **집계 주기가 곧 통계 신선도다.** `cron`을 줄이면 더 자주 갱신되지만 그만큼 집계 쿼리가 늘어난다. 지원자
  수에 비례해 집계를 돌리지 않는 이유는, 그렇게 하면 마감 직전 몰림 구간에서 집계 횟수가 지원 건수에 비례해
  늘어나 사전 계산을 도입한 이유가 사라지기 때문이다.
- `precompute.enabled: false`로 두면 조회 시점에 매번 집계한다. **운영에서는 켜 둔다.** 통계는 비로그인
  공개 API이고 조회 빈도 제한이 없어, 사전 계산 캐시가 사실상 유일한 부하 방어 수단이다.
- Redis가 죽어도 통계 조회는 실패하지 않는다. 캐시 접근이 실패하면 조회 경로에서 직접 집계한다. 다만 이때는
  부하 방어가 사라지므로, Redis 장애가 길어지면 `enabled`와 무관하게 DB 부하를 확인해야 한다.
- 다중 인스턴스에서는 Redis 선점 잠금(`statistics:v1:lock:form:*`)으로 중복 집계를 막는다. `lock-ttl`은
  한 지원폼의 집계가 끝나는 데 걸리는 시간보다 넉넉히 잡는다.
- 집계 규칙을 바꿔 기존 캐시를 그대로 쓰면 안 될 때는 `StatisticsCacheStore.KEY_PREFIX`의 버전(`v1`)을
  올린다. 예전 엔트리는 TTL로 자연히 사라진다.

## 관련 문서

- 운영 DB 마이그레이션: `src/main/resources/db/migration/README.md`
