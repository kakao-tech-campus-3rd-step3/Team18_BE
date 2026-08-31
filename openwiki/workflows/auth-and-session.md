---
type: workflow-concept
title: 카카오 로그인과 인증 세션
description: 카카오 OAuth2 인가 코드 교환부터 회원가입/로그인, 세 종류의 JWT(임시/액세스/리프레시), Redis 기반 리프레시 토큰 저장·로그아웃 블랙리스트, JWT 인증 필터와 메서드 단위 인가까지 전체 인증 흐름을 설명한다.
tags: [oauth2, kakao, jwt, redis, spring-security, authentication]
verified:
  - by: openwiki/0.4.3
    at: 2026-08-31T14:35:03.507Z
sources:
  - id: openwiki-source-6889afbdd5cba4dd4ea8cfc9
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/domain/auth/entity/RefreshToken.java
  - id: openwiki-source-318b1baf4520a37f97c350c2
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/domain/auth/service/AuthServiceImpl.java
  - id: openwiki-source-d342c70954da479f97296d9d
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/global/security/CustomSecurityService.java
  - id: openwiki-source-9a2c8823d39d6304894ffb5a
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/global/security/JwtAuthenticationFilter.java
  - id: openwiki-source-d4222661d0cb93cb4fca0922
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/global/security/JwtProvider.java
generated: { by: "claude-code", at: "2026-08-31T14:35:03.507Z" }
---

# 카카오 로그인과 인증 세션

이 서비스는 자체 회원가입 화면 없이 카카오 로그인만으로 사용자를 식별하되, 최초 1회는 추가 정보(학번 등)를
입력받아 계정을 완성시킨다. 인증은 완전히 무상태(stateless)이며, 세션 대신 JWT와 Redis만으로 로그인 상태를
관리한다.

## 카카오 로그인 → 회원가입/로그인 분기

`AuthServiceImpl.kakaoLogin`(`domain/auth/service/AuthServiceImpl.java`)이 전체 흐름의 시작점이다.

1. 프론트에서 받은 인가 코드(authorization code)로 카카오 토큰 엔드포인트에 액세스 토큰을 요청한다
   (`RestClient`, [[overview]]에서 다루는 타임아웃 설정 적용).
2. 카카오 액세스 토큰으로 사용자 정보(카카오 ID, 닉네임)를 조회한다.
3. `kakaoId`로 기존 `User`를 조회해 존재 여부로 분기한다.
   - **기존 회원**: 정식 액세스/리프레시 토큰을 즉시 발급하고 `LoginSuccessResponseDto`를 반환한다.
   - **신규 회원**: 5분(`temporaryTokenValidityInSeconds`)짜리 임시 토큰을 발급해 `RegistrationRequiredResponseDto`로
     돌려주고, 프론트가 추가 정보(학번/이름/전화번호/이메일/학과)를 입력받아 `/register`를 호출하게 한다.
4. `AuthServiceImpl.register`는 임시 토큰이 `TokenType.TEMPORARY`인지 검증한 뒤, 학번으로 기존 `User`가
   있으면 카카오 계정을 연결(`connectKakaoId`, 전화번호 일치 검증 포함)하고, 없으면 새 `User`를 생성한다.
   이미 다른 카카오 계정에 연동된 학번이면 `DuplicateKakaoIdException`으로 계정 탈취 시도를 차단한다.

## JWT 세 종류

`JwtProvider`(`global/security/JwtProvider.java`)는 `TokenType`(TEMPORARY/ACCESS/REFRESH)에 따라 서로 다른
클레임 구성의 토큰을 HS512로 서명해 발급한다.

- **임시 토큰**: `subject=TEMPORARY`, `kakaoId`/`nickname` 클레임 포함, 유효기간 5분. 회원가입 완료 전까지만
  사용되며 API 접근 권한은 없다.
- **액세스 토큰**: `subject=userId`, `memberships` 클레임에 사용자가 속한 모든 동아리의 `{clubId: role}` 맵을
  담아, 매 요청마다 DB 조회 없이 인가 판단을 할 수 있게 한다. 유효기간은 `jwt.access-token-validity-in-seconds`
  (기본 1800초/30분).
- **리프레시 토큰**: `subject=userId`, 유효기간 `jwt.refresh-token-validity-in-seconds`(기본 604800초/7일).
  발급 시마다 Redis(`RefreshToken`, `domain/auth/entity/RefreshToken.java`)에 `@RedisHash` + `@TimeToLive`로
  저장되어 자연 만료된다.

## 재발급(reissue)과 로그아웃

`AuthServiceImpl.reissue`는 리프레시 토큰을 검증(서명·만료·`tokenType=REFRESH`)한 뒤, Redis에 저장된 토큰과
요청 토큰이 일치하는지 재확인한다. 일치하지 않으면 `InvalidRefreshTokenException`, Redis에 아예 없으면
(이미 로그아웃됨) `LoggedOutUserException`을 던진다. 검증을 통과하면 액세스·리프레시 토큰을 모두 새로 발급하고
Redis 값을 덮어써(rotation) 이전 리프레시 토큰을 무효화한다.

`AuthServiceImpl.logout`은 액세스 토큰의 남은 유효 시간(TTL)만큼 `blacklist:{accessToken}` 키를 Redis에
저장해 즉시 무효화하고, 사용자의 리프레시 토큰을 Redis에서 삭제하며, `refreshToken` 쿠키를 `Max-Age=0`으로
덮어써 브라우저에서도 제거한다. 액세스 토큰 자체는 만료 전까지 서명상 유효하므로, 블랙리스트가 없으면
로그아웃 후에도 같은 토큰이 재사용될 수 있다는 점이 이 설계의 핵심 전제다.

## 요청마다의 인증: `JwtAuthenticationFilter`

`JwtAuthenticationFilter`(`global/security/JwtAuthenticationFilter.java`)는 `/api/auth/reissue`와
`/api/auth/kakao/login`을 제외한 모든 요청에서 `Authorization: Bearer ...` 헤더를 검사한다.

1. 토큰이 `blacklist:{token}` 키로 Redis에 있으면 즉시 `InvalidJwtException(BLACKLISTED_TOKEN)`으로 거부한다.
2. 서명 검증 후 `tokenType`이 `ACCESS`가 아니면(예: 리프레시 토큰으로 API를 호출한 경우) 인증을 세팅하지
   않고 다음 필터로 넘긴다 — 이후 `anyRequest().authenticated()`에 걸려 401이 된다.
3. `memberships` 클레임과 `subject`(userId)로 `PrincipalDetailsService.loadUserByUsername`을 호출해
   `UserDetails`를 만들고 `SecurityContextHolder`에 인증 정보를 세팅한다.
4. `SignatureException`/`MalformedJwtException`/`ExpiredJwtException`/`UnsupportedJwtException`/
   `IllegalArgumentException`을 각각 별도의 `ErrorCode`로 매핑해 `HandlerExceptionResolver`를 통해
   [[error-handling]]의 `GlobalExceptionHandler` 경로로 넘긴다.

## 메서드 단위 인가: `CustomSecurityService`

액세스 토큰의 `memberships` 클레임은 `CustomSecurityService`(`global/security/CustomSecurityService.java`)를
통해 `@PreAuthorize` 표현식에서 직접 활용된다. `isClubAdminOrExecutive(clubId)`는 DB 조회 없이 토큰 클레임만
으로 판단하는 반면, `isClubAdminOrExecutiveForApplication(applicationId)`는 지원서가 속한 클럽 ID를 조회한
뒤 같은 클레임 기반 판단을 적용한다. 두 메서드 모두 `CLUB_ADMIN` 또는 `CLUB_EXECUTIVE` 역할만 허용한다. 이
경로에서 발생하는 `AuthorizationDeniedException`은 [[error-handling]]에서 설명한 대로
`ForbiddenAccessException`으로 변환되어 동일한 에러 응답 형식을 유지한다.

## Redis 커맨드 타임아웃과의 상호작용

[[overview]]에서 다루듯 `RedisConfig`는 Lettuce 커맨드 타임아웃을 최대 3초로 강제한다. 인증 필터의 블랙리스트
조회, 리프레시 토큰 조회/저장이 모두 이 타임아웃의 영향을 받으므로, Redis 장애 시 인증 관련 요청이 무한정
대기하지 않고 빠르게 실패하도록 설계되어 있다.
