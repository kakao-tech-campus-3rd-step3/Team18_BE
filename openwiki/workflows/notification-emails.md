---
type: workflow-concept
title: 전형 결과 이메일 알림
description: 지원서 제출과 각 전형 단계 결과를 트랜잭션 커밋 이후 비동기 도메인 이벤트로 처리해 HTML 이메일을 발송하고, SMTP 실패를 임시/영구로 분류해 Spring Retry로 재시도하는 알림 시스템을 설명한다.
tags: [email, event-driven, spring-retry, smtp, async]
verified:
  - by: openwiki/0.4.3
    at: 2026-08-31T14:35:03.507Z
sources:
  - id: openwiki-source-3ae00028e502e4399a3ffd36
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/domain/email/eventListener/ApplicationNotificationListener.java
  - id: openwiki-source-861afc2ae10d313a059e0396
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/domain/email/sender/SmtpEmailSender.java
  - id: openwiki-source-ed9ad8cbc255938e2f50d9d6
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/domain/email/sender/SmtpFailureClassifier.java
  - id: openwiki-source-cbaad3eed56f31e1afb4c33e
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/domain/email/service/EmailService.java
generated: { by: "claude-code", at: "2026-08-31T14:35:03.507Z" }
---

# 전형 결과 이메일 알림

지원서 제출 확인과 전형 단계별 결과(면접 합격/불합격, 최종 합격/불합격) 통지는 서비스 로직에서 직접 메일을
보내지 않고, 도메인 이벤트를 발행한 뒤 트랜잭션 커밋 이후 별도 스레드에서 비동기로 처리한다.

## 이벤트 발행과 트랜잭션 커밋 이후 처리

`ApplicationNotificationListener`(`domain/email/eventListener/ApplicationNotificationListener.java`)는
`ApplicationSubmittedEvent`/`InterviewApprovedEvent`/`InterviewRejectedEvent`/`FinalApprovedEvent`/
`FinalRejectedEvent` 다섯 종류의 이벤트를 각각 별도 메서드로 수신한다. 모든 리스너 메서드는
`@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`로 선언되어, 지원서 상태 변경 트랜잭션이
실제로 커밋된 뒤에만 실행된다 — 트랜잭션이 롤백되면 이메일도 전혀 발송되지 않는다. 동시에 `@Async`
([[overview]]에서 다루는 `AsyncConfig`의 `@EnableAsync`로 활성화)로 별도 스레드에서 실행되어, 이메일 발송
지연이나 실패가 원래 API 요청의 응답 시간에 영향을 주지 않는다.

## 템플릿 렌더링과 발송: `EmailService`

`EmailService`(`domain/email/service/EmailService.java`)는 이벤트 종류별로 `EmailTemplateRenderer`에
모델(동아리명·지원자명·학번·학과·전화번호·답변 목록 등)을 채워 `src/main/resources/templates/`의 Thymeleaf
HTML 템플릿을 렌더링한다. `ResultType`(INTERVIEW_APPROVED/INTERVIEW_REJECTED/FINAL_APPROVED/FINAL_REJECTED)에
따라 제목과 템플릿 파일을 매핑하며, 합격 통지(`INTERVIEW_APPROVED`/`FINAL_APPROVED`)는 `message`가 비어
있으면 `IllegalArgumentException`으로 즉시 거부해 빈 합격 메시지가 발송되는 것을 막는다. 발신자는
`spring.email.from` 설정값을 쓰고, 회신 주소(`replyTo`)는 해당 동아리 회장의 이메일로 설정되어 지원자가
답장하면 동아리 운영진에게 전달된다.

## SMTP 발송과 실패 분류: 재시도 vs 영구 실패

`SmtpEmailSender`(`domain/email/sender/SmtpEmailSender.java`)는 `JavaMailSender`로 실제 SMTP 발송을
수행하며, `RetryConfig`(`@EnableRetry`)가 활성화한 `@Retryable`로 감싸져 있다. 최대 5회, 2초 초기 지연에
배수 2.0(최대 60초)의 지수 백오프로 재시도하지만, 재시도 대상은 `RetryableEmailException`으로 분류된
실패로 한정된다.

`SmtpFailureClassifier`(`domain/email/sender/SmtpFailureClassifier.java`)가 이 분류를 담당한다.

- **일시 실패(재시도)**: 연결 실패(`MailConnectException`)나 타임아웃(`SocketTimeoutException`), SMTP
  확장 상태 코드(RFC 3463)가 `4.x.x`인 경우, `5.2.2`(수신함 용량 초과)인 경우, 또는 기본 응답 코드가
  4xx·552(용량/스토리지)인 경우를 임시 실패로 판단해 `RetryableEmailException`을 던진다.
- **영구 실패**: 인증 실패(`MailAuthenticationException`)나 그 외 5.x.x 확장 코드/5xx 기본 코드는 즉시
  `EmailSendFailedException`으로 던져 재시도하지 않는다.
- 5회 재시도를 모두 소진하면 `@Recover` 메서드가 호출되어 `EmailSendFailedException`으로 최종 실패 처리하고
  에러 로그를 남긴다.

`SmtpFailureClassifier.toErrorCode`는 같은 분류 정보를 [[error-handling]]의 `ErrorCode` 체계(예:
`EMAIL_RECIPIENT_INVALID`, `EMAIL_AUTH_FAILED`, `EMAIL_POLICY_REJECTED`, `EMAIL_TIMEOUT`)로 변환해, 발송
실패 원인을 표준 에러 응답 형식으로도 노출할 수 있게 한다.
