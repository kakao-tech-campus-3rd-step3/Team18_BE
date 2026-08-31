---
type: architecture-concept
title: 예외 처리와 에러 응답
description: CustomException 계층과 ErrorCode 카탈로그, GlobalExceptionHandler가 이를 일관된 HTTP 에러 응답으로 변환하는 방식, Spring Security 인증/인가 실패가 같은 경로로 합류하는 구조를 설명한다.
tags: [error-handling, exception, spring-security, rest-api]
verified:
  - by: openwiki/0.4.3
    at: 2026-08-31T14:35:03.507Z
sources:
  - id: openwiki-source-779659faa5c4f10efc5eeef8
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/global/config/SecurityConfig.java
  - id: openwiki-source-b3cbfad05df21c73729a908b
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/global/exception/dto/ErrorResponseDto.java
  - id: openwiki-source-35c074aada5a85867ff29f32
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/global/exception/exceptions/CustomException.java
  - id: openwiki-source-2bd33da40100b63173674c6e
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/global/exception/exceptions/UserNotFoundException.java
  - id: openwiki-source-f85e5804ebdf0b1659e785df
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/global/exception/handler/GlobalExceptionHandler.java
generated: { by: "claude-code", at: "2026-08-31T14:35:03.507Z" }
---

# 예외 처리와 에러 응답

이 서비스는 "예측 가능한 비즈니스 예외"를 모두 하나의 계층(`CustomException`)과 하나의 카탈로그(`ErrorCode`)로
표준화하고, `@RestControllerAdvice`인 `GlobalExceptionHandler`
(`global/exception/handler/GlobalExceptionHandler.java`)에서 이를 일관된 JSON 응답으로 변환한다.

## 정의 → 발생 → 처리 흐름

1. **정의**: `ErrorCode`(`global/exception/code/ErrorCode.java`)에 메시지와 `HttpStatus`를 묶어 상수로 정의한다.
   400(잘못된 요청)부터 503/504(외부 연동 타임아웃·일시 오류)까지 HTTP 상태 코드별로 그룹화되어 있으며,
   카카오 API 타임아웃(`KAKAO_API_TIMEOUT`), SMTP 관련 오류(`EMAIL_AUTH_FAILED`, `EMAIL_TIMEOUT`,
   `EMAIL_TEMPORARY_FAILURE` 등)처럼 외부 시스템 실패도 도메인 예외와 동일한 방식으로 분류된다.
2. **발생**: `CustomException`(`global/exception/exceptions/CustomException.java`)을 직접 던지기보다, 이를
   상속한 구체 클래스(`UserNotFoundException`, `ClubNotFoundException`, `ForbiddenAccessException` 등
   `global/exception/exceptions/` 아래 50여 개)를 서비스 계층에서 던진다. 각 구체 클래스는 생성자에서 고정된
   `ErrorCode`를 부모에 전달하고, 선택적으로 `detail`(디버깅용 상세 문자열)을 함께 전달한다.
3. **처리**: `GlobalExceptionHandler.handleCustomException`이 `ErrorCode`와 `detail`을 꺼내
   `ErrorResponseDto`(`global/exception/dto/ErrorResponseDto.java`)로 감싸고, `ErrorCode`에 매핑된
   `HttpStatus`로 응답한다. `detail`이 있으면 `ErrorResponseDto.of`, 없으면 `ErrorResponseDto.from`을 사용해
   내부 정보 노출 여부를 명시적으로 구분한다.

## 표준 에러 응답 형식

```json
{
  "error_code": "USER_NOT_FOUND",
  "message": "해당 유저가 존재하지 않습니다.",
  "detail": "학번: 20231234"
}
```

`ErrorResponseDto`는 `@JsonInclude(NON_NULL)`이므로 `detail`이 없으면 응답 JSON에서 아예 생략된다.

## CustomException 외에 별도로 처리되는 경우

`GlobalExceptionHandler`는 `CustomException` 계열 외에도 프레임워크 레벨 예외를 개별 핸들러로 잡아 같은 형식의
응답으로 변환한다.

- `MethodArgumentNotValidException`(`@Valid` 검증 실패): `BindingResult`의 필드 에러를 `"field: message, ..."`
  형태 문자열로 합쳐 `INVALID_INPUT_VALUE`의 `detail`로 반환한다.
- `MethodArgumentTypeMismatchException`/`ConversionFailedException`: 요청 파라미터를 `Status` enum으로
  변환하는 데 실패했거나 원인 체인에 `StatusNotFoundException`이 있으면 `STATUS_NOT_FOUND`로, 그 외에는
  `INVALID_INPUT_VALUE`로 응답한다.
- `HttpMessageNotReadableException`(요청 바디 역직렬화 실패, 예: 정의되지 않은 enum 값): 이 핸들러가 없으면
  전역 `Exception` 핸들러로 떨어져 500으로 나가므로, 클라이언트 입력 오류를 400으로 명확히 구분하기 위해
  별도로 잡아 `INVALID_INPUT_VALUE`로 응답한다.
- `MaxUploadSizeExceededException`: `TOO_LARGE_FILE`(400)로 변환한다.
- `AuthorizationDeniedException`(`@PreAuthorize` 메서드 시큐리티 인가 실패): `ForbiddenAccessException`으로
  변환한 뒤 `handleCustomException`을 재사용해 동일한 응답 형식을 유지한다.
- 그 외 처리되지 않은 모든 `Exception`은 `handleException`이 잡아 `INTERNAL_SERVER_ERROR`(500)로 응답하고
  스택 트레이스를 `log.error`로 남긴다.

## Spring Security 인증/인가 실패가 같은 경로로 합류하는 방식

`SecurityConfig`(`global/config/SecurityConfig.java`)는 인증 실패와 인가 실패를 직접 HTTP 응답으로 쓰지 않고,
`HandlerExceptionResolver`(`resolver.resolveException(...)`)에 위임해 `GlobalExceptionHandler`로 되돌려 보낸다.

- `authenticationEntryPoint()`는 인증되지 않은 요청에 대해 `UnauthenticatedUserException`을 발생시켜 넘긴다.
- `accessDeniedHandler()`는 인가 실패에 대해 `ForbiddenAccessException`을 발생시켜 넘긴다.

두 예외 모두 `CustomException`의 하위 클래스이므로, 필터 체인에서 발생한 인증/인가 실패도 컨트롤러에서 던진
비즈니스 예외와 완전히 동일한 `handleCustomException` 경로를 타 동일한 형식의 에러 응답을 만든다. 이 방식
덕분에 예외 처리 로직이 필터/컨트롤러 경계에 상관없이 하나로 통일된다.
