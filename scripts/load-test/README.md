# 실시간 인기 동아리 로컬 부하 테스트

운영 환경이 아닌 폐기 가능한 로컬 애플리케이션·Redis·MySQL 환경에서만 실행합니다.

## 실행

```bash
k6 run \
  -e BASE_URL=http://localhost:8080 \
  -e CLUB_ID=1 \
  scripts/load-test/club-popularity.js
```

빠른 연결 확인은 다음처럼 20초 단축 시나리오로 실행합니다.

```bash
k6 run \
  -e BASE_URL=http://localhost:8080 \
  -e CLUB_ID=1 \
  -e LOAD_TEST_SHORT=true \
  scripts/load-test/club-popularity.js
```

테스트 데이터가 없는 환경에서는 상세 조회의 `404`를 정상적인 응답으로 취급하고, 기록·heartbeat는 `204`, 인기 목록은 `200`을 기대합니다.

설정값은 낮은 부하에서 시작해 단계적으로 조정합니다. 테스트 중 다음을 함께 관찰합니다.

- API p50·p95·p99와 오류율
- Redis 오류 및 pending 큐 개수·최고 대기 시간
- DB flush 저장·실패 건수와 소요 시간
- 복구 소요 시간, 연결 풀·잠금·느린 쿼리
- 애플리케이션 CPU·메모리

부하 테스트 결과에는 환경, 요청 조합, 단계별 부하, 주요 지표, 발견 문제, 설정 조정과 재검증 결과만 기록하고 비밀값·대량 로그 원문은 저장하지 않습니다.
