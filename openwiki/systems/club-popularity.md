---
type: system-concept
title: 동아리 인기도 실시간 집계
description: 조회수·체류시간(heartbeat)을 Redis 정렬 집합과 Lua 스크립트로 원자적으로 집계해 실시간 인기 동아리를 산정하고, 주기적으로 MySQL에 영속화하며 Redis 장애로부터 복구하는 시스템을 설명한다.
tags: [redis, lua, club-popularity, real-time, scheduler, recovery]
verified:
  - by: openwiki/0.4.3
    at: 2026-08-31T14:35:03.507Z
sources:
  - id: openwiki-source-5fad04e461bde8cbcb125cad
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/domain/clubPopularity/service/ClubPopularityQueryService.java
  - id: openwiki-source-37b587638b5d4423b4116b0d
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/domain/clubPopularity/service/ClubPopularityRecordingService.java
  - id: openwiki-source-26794160819dfa16726dbd8c
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/domain/clubPopularity/service/ClubPopularityRecoveryService.java
  - id: openwiki-source-01d18e11eb309c8c409ac237
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/global/scheduler/ClubPopularityScheduler.java
  - id: openwiki-source-a077662397ec6ca2598ad6eb
    resource: repo://src/main/resources/lua/club-popularity-aggregate.lua
  - id: openwiki-source-a5039bb0aa70fd7354157f1a
    resource: repo://src/main/resources/lua/club-popularity-record-views.lua
generated: { by: "claude-code", at: "2026-08-31T14:35:03.507Z" }
---

# 동아리 인기도 실시간 집계

동아리 상세 페이지 조회(view)와 일정 간격의 체류 신호(heartbeat)를 Redis에 원자적으로 기록해, 최근 24시간
조회자 수와 현재 활성 조회자 수 두 지표로 "인기 동아리" 배지를 실시간 산정하는 시스템이다. 기록은 항상
Redis에만 쓰고, 별도 스케줄러가 주기적으로 MySQL(`ClubView`)에 영속화한다.

## 기록 경로: 조회수와 heartbeat

`ClubPopularityRecordingService`(`domain/clubPopularity/service/ClubPopularityRecordingService.java`)가
`recordView`/`recordHeartbeat` API의 진입점이다. 먼저 `ClubPopularityViewerResolver`로 인증된 사용자 또는
익명 ID(`anonymousId`)를 하나의 뷰어 식별자로 정규화하고, 이후 실제 기록은 Redis Lua 스크립트에 위임한다.

- `club-popularity-record-views.lua`(`src/main/resources/lua/`): 복구 상태가 `READY`가 아니면 3(RECOVERING)을,
  요청한 클럽 ID가 `KNOWN_CLUBS` 집합에 없으면 4(INVALID_CLUB)를 즉시 반환한다. 클럽별 rate-limit 키가 이미
  있으면 2(RATE_LIMITED)를 반환하고, 그렇지 않으면 rate-limit 키를 설정한 뒤 최근 조회자(`recent`)/활성
  조회자(`active`) 정렬 집합에 `ZADD ... GT`(기존 점수보다 클 때만 갱신)로 시각을 기록하고, `pending` 정렬
  집합에도 같은 이벤트를 추가해 1(RECORDED)을 반환한다.
- `club-popularity-record-heartbeat.lua`: 같은 구조로 활성 조회자 집합만 갱신한다(최근 조회자 집합은
  건드리지 않음).
- 두 스크립트 모두 클럽 ID를 `CANDIDATES` 집합에 추가해, 이후 조회 시 어떤 클럽을 재집계 대상으로 삼을지
  결정하는 데 사용한다.

`ClubPopularityRecordingService.RecordingResult`는 Redis 결과를 `RECORDED`/`RATE_LIMITED`/`RECOVERING`/
`INVALID_CLUB`/`REDIS_ERROR`/`INVALID_IDENTITY`/`DISABLED`로 매핑하며, `DataAccessException`이 나면
`REDIS_ERROR`로 흡수해 조회 기능 자체가 Redis 문제로 실패하지 않게 한다.

## 조회 경로: 인기 동아리 판정

`ClubPopularityQueryService.getPopularClubs()`는 복구 상태가 `READY`일 때만 동작한다. `CANDIDATES`와
`KNOWN_CLUBS`의 교집합을 후보로 삼아, `club-popularity-aggregate.lua`를 파이프라인으로 일괄 실행한다. 이
스크립트는 각 클럽의 최근/활성 정렬 집합에서 컷오프 이전 항목을 `ZREMRANGEBYSCORE`로 제거한 뒤 남은 개수를
`ZCOUNT`로 세고, 두 카운트가 모두 0이면 해당 클럽을 `CANDIDATES`에서 제거해 다음 집계 대상에서 자연스럽게
빠지게 한다. 결과는 `recentCount * 2^32 + activeCount` 형태로 하나의 `Long`에 패킹되어 반환된다.
`recentViewerCount >= recentViewerThreshold` 또는 `activeViewerCount >= activeViewerThreshold`(각각
`ClubPopularityProperties`의 설정값, 기본 10/3)이면 인기 배지가 붙는다. 집계 도중 복구 상태로 전환되면
부분 결과를 노출하지 않고 빈 응답을 반환한다.

## 영속화: 스케줄 기반 플러시

`ClubPopularityScheduler.flushPending()`(`global/scheduler/ClubPopularityScheduler.java`)은
`club-popularity.flush-interval-minutes`(기본 60분)마다 실행되며, 복구 상태가 `READY`일 때만 동작한다.
`ClubPopularityPersistenceService.flushPending`을 배치 단위로 반복 호출해 `pending` 정렬 집합의 이벤트를
MySQL `ClubView`로 옮기고, 한 번 실행에서 처리할 최대 레코드 수(`flushMaxRecordsPerRun`)·최대 DB 시도
횟수·최대 실행 시간(`flushMaxDurationMinutes`) 세 가지 상한을 모두 지키며 중단한다. DB 예외가 나면
`transientRetryDelaysSeconds`(기본 1/3/10초)만큼 대기 후 재시도하고, 그래도 반복 실패하는 레코드는
실패 큐(`FAILED_RETRY`)로 옮겨 `failedRecordRetryDelaysMinutes`(기본 10/60/360분) 간격으로 별도 재시도한다.
같은 스케줄러의 `cleanupOldViews()`는 `retentionHours`(기본 25시간)보다 오래된 `ClubView` 행과 만료된 실패
레코드를 배치 삭제한다.

## Redis 장애로부터의 복구

`ClubPopularityRecoveryService`(`domain/clubPopularity/service/ClubPopularityRecoveryService.java`)는
애플리케이션 시작 시(`ApplicationReadyEvent`)와 `recovery-check-interval-minutes`(기본 5분)마다 복구가
필요한지 확인한다. 복구가 필요하면 분산 락(`RECOVERY_LOCK`, `SETNX` + TTL)을 획득한 뒤 상태를 `RECOVERING`으로
표시하고, `KNOWN_CLUBS` 집합을 DB의 현재 클럽 ID 전체로 원자적 교체(`REPLACE_KNOWN_CLUBS_SCRIPT`, RENAME 기반)한
다음, `ClubView` 테이블에서 `recentViewerWindowHours`(기본 24시간) 이내 조회 기록을 페이지 단위로 읽어 최근
조회자 정렬 집합을 재구성한다. 이 과정에서 주기적으로 락 소유권을 확인·갱신(`refreshRecoveryLock`)하고,
`recoveryMaxDurationMinutes`(기본 30분)를 넘기면 타임아웃으로 중단한다. 복구가 끝나면 활성 조회자 집합은
전부 비우고(초 단위로 만료되는 값이라 재구성할 필요가 없음) 상태를 `READY`로 전환한다. 이 설계 덕분에
Redis가 재시작되어 데이터가 사라져도, 최근 조회자 집합은 MySQL에 남아 있는 이력으로부터 다시 채워진다.
