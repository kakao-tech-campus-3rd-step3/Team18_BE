---
type: system-concept
title: 지원자 통계 집계
description: 동아리 지원폼 단위로 성별·학부·입학연도·일자별 지원 추이를 집계하고, 재식별 방지를 위한 최소 공개 기준으로 마스킹하며, Redis cache-aside로 공개 통계를 캐싱하는 시스템을 설명한다.
tags: [statistics, redis-cache, privacy, re-identification]
verified:
  - by: openwiki/0.4.3
    at: 2026-08-31T14:35:03.507Z
sources:
  - id: openwiki-source-166b88b01a6ca11e30d9001a
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/domain/statistics/cache/RedisStatisticsCache.java
  - id: openwiki-source-f1704701c4befd456a52a352
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/domain/statistics/config/StatisticsProperties.java
  - id: openwiki-source-f12eff50175b0aedf8311780
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/domain/statistics/controller/StatisticsAdminController.java
  - id: openwiki-source-2a534c441899c1921bfd9c01
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/domain/statistics/controller/StatisticsController.java
  - id: openwiki-source-00090889816c1a6d64074184
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/domain/statistics/service/StatisticsAggregator.java
  - id: openwiki-source-1b989a423647c01dcebabcfc
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/domain/statistics/service/StatisticsServiceImpl.java
generated: { by: "claude-code", at: "2026-08-31T14:35:03.507Z" }
---

# 지원자 통계 집계

지원자 통계는 동아리(정확히는 1:1 관계인 `ClubApplyForm`) 단위로 4가지 항목(`StatisticsDimension`:
GENDER/FACULTY/ADMISSION_YEAR/DAILY_APPLICATIONS)을 집계한다. 공개(비로그인) 통계와 관리자 통계는 같은
집계 로직을 공유하되, 공개 경로에만 최소 공개 기준 마스킹과 Redis 캐시가 적용된다.

## 집계: `StatisticsAggregator`

`StatisticsAggregator`(`domain/statistics/service/StatisticsAggregator.java`)는 dimension마다 별도 쿼리로
집계해 교차 집계로 인한 조합 폭발을 피한다.

- **GENDER/FACULTY**: 각각 `Gender`/`Faculty` enum 선언 순서로 버킷을 정렬하고, 값이 `null`인 지원자(성별·학부
  필드가 추가되기 전에 생성됐거나 미입력)는 항상 마지막 "미입력" 버킷으로 합산한다.
- **ADMISSION_YEAR**: 학번에서 입학연도를 추출(`AdmissionYearBucketer`)해, 기준 연도(KST 기준 현재 연도)로부터
  최근 6년(`ADMISSION_YEAR_RECENT_YEARS`)은 연도별 개별 버킷으로, 그보다 오래된 연도는 "그 이전" 하나로
  묶는다. 6자리 숫자가 아닌 학번은 "미입력"으로 분류한다.
- **DAILY_APPLICATIONS**: 지원폼이 속한 동아리의 모집 시작·종료일 사이에서 일자별 지원 건수를 집계하되,
  지원자가 없는 날도 `count:0`으로 채운다. 단, 아직 오지 않은 미래 날짜는 채우지 않는다(0 채움 종료일은
  `min(모집종료일, 오늘)`). 이 dimension은 시계열이라 비율(ratio)을 계산하지 않는다.

## 재식별 방지: 최소 공개 기준

`StatisticsServiceImpl.getStatistics`(`domain/statistics/service/StatisticsServiceImpl.java`)는 전체
지원자 수(`totalApplicants`)가 `StatisticsProperties.minTotalApplicants()`(기본 3명) 미만이면 분포 데이터를
전혀 내려주지 않고 `masked=true`, 빈 `results`로 응답한다. 기준은 전체 지원자 수에만 걸리고 개별 버킷에는
걸지 않는데, 이는 버킷별로 따로 마스킹하면 "버킷 간 뺄셈"으로 마스킹된 소수 인원의 속성을 역산할 수 있기
때문이다. `getStatisticsForAdmin`은 이 마스킹을 거치지 않고 항상 원본 분포를 반환한다.

일관성을 위해, 마스킹 여부 판정과 마스킹 해제 시 반환되는 `totalApplicants`/`calculatedAt`은 모두 같은
캐시 스냅샷 값을 사용한다. 서비스 클래스 전체가
`@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)`로 지정되어, 같은 요청 안에서 총
지원자 수 판정과 dimension 집계가 동일한 스냅샷을 보도록 강제한다.

## Redis 캐시: cache-aside

`RedisStatisticsCache`(`domain/statistics/cache/RedisStatisticsCache.java`)는 공개 통계 조회 경로에서만
쓰이는 cache-aside 캐시다. 캐시 키는 `statistics:v1:{clubApplyFormId}`이며, 마스킹하지 않은 전체 dimension
결과를 JSON으로 저장하고 `StatisticsProperties.cacheTtlSeconds()`(기본 1800초/30분) TTL로 만료시킨다.
마스킹과 dimension 필터링(subset 선택)은 캐시에서 꺼낸 뒤 서빙 시점에 적용하므로, 같은 지원폼에 대해
어떤 dimension 조합을 요청해도 캐시는 한 번만 채워진다. 캐시 조회/저장 중 예외(Redis 장애, 역직렬화 실패
등)는 모두 삼켜서 조회는 "미스"로, 저장은 "무시"로 처리해 캐시 문제가 통계 조회 자체를 막지 못하게 한다.
스케줄러 없이 요청이 들어올 때만 채워지는 순수 cache-aside 방식이다.

## 공개 통계 API 접근 제어

`SecurityConfig`는 `GET /api/clubs/*/statistics`를 공개 API로 허용한다(지원자에게 공개하는 통계이므로
비로그인 조회를 허용). 반면 관리자 전용 엔드포인트(`StatisticsAdminController`)는 별도 경로
(`/api/clubs/*/statistics/admin`)로 분리되어 있어, 하나의 세그먼트 매처로도 공개 경로와 관리자 경로가
서로 겹치지 않는다.
