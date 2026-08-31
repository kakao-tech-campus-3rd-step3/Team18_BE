---
type: workflow-concept
title: 동아리 지원 및 전형 프로세스
description: 지원폼 질문 구성부터 지원서 제출·답변 검증, 서류(INTERVIEW)·면접(FINAL)·결과(RESULT) 3단계 전형에 따른 Status/Stage 전이, 그리고 최종 합격자가 ClubMember(CLUB_MEMBER)로 전환되는 전체 흐름을 설명한다.
tags: [application, recruitment, stage-transition, club-member, form-question]
verified:
  - by: openwiki/0.4.3
    at: 2026-08-31T14:35:03.507Z
sources:
  - id: openwiki-source-ef1d8e0bbe99c5870231e315
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/domain/application/service/ApplicationServiceImpl.java
generated: { by: "claude-code", at: "2026-08-31T14:35:03.507Z" }
---

# 동아리 지원 및 전형 프로세스

동아리 하나에 지원폼(`ClubApplyForm`) 하나가 있고, 지원자는 그 폼에 답변(`Answer`)을 채워 지원서
(`Application`)를 제출한다. 지원서는 서류(`INTERVIEW`) → 면접(`FINAL`) → 결과(`RESULT`) 세 단계
(`Stage`)를 거치며, 각 단계에서 합격/불합격(`Status`)이 정해진다. 최종 합격자는 `ClubMember`의 역할이
`APPLICANT`에서 `CLUB_MEMBER`로 바뀌면서 정식 동아리원이 된다.

## 지원폼 질문 구성

`FormQuestion`(`domain/formQuestion/entity/FormQuestion.java`)의 `FieldType`은 `TEXT`/`RADIO`/`CHECKBOX`/
`TIME_SLOT` 네 가지다. `ClubApplyForm`은 동아리당 하나이며, `interviewMessage`/`finalMessage` 두 필드에
각 단계 발표 시 지원자에게 보여줄 안내 문구를 담아 둔다([[domain-model]] 참고).

## 지원서 제출: `submitApplication`

`ApplicationServiceImpl.submitApplication`(`domain/application/service/ApplicationServiceImpl.java`)은
학번(`studentId`)으로 `User`를 조회하거나 새로 만든다. 이미 다른 학번으로 등록된 이메일/전화번호로 제출하면
`ExistingUserEmailException`/`ExistingUserPhoneNumberException`을 던지고, 반대로 나머지 정보가 다른데
학번만 겹치면 `ExistingUserStudentIdException`을 던진다. 기존 `User`를 재사용하는 경우, 비어 있는 성별/학부
필드는 조건부 UPDATE(`updateGenderIfAbsent`/`updateFacultyIfAbsent`)로 원자적으로 채운다([[domain-model]]의
"없을 때만 채운다" 규칙 참고). 같은 폼에 이미 지원 이력이 있으면 `overwrite` 플래그에 따라 기존 지원서를
덮어쓰거나, 그렇지 않으면 이미 제출됨을 알리는 응답만 반환한다.

지원서가 새로 생성될 때(`createApplication`), `Application`과 동시에 `ClubMember`가
`role=APPLICANT, activeStatus=ACTIVE`로 즉시 생성되어 해당 `Application`을 참조한다. 즉 지원자는 합격
여부와 무관하게 지원 시점부터 이미 `ClubMember` 레코드를 갖는다 — 이후 전형 단계 처리에서 이 레코드의
`role`이 갱신되거나(합격), 연결이 끊어진 뒤 `Application`이 삭제된다(불합격).

## 답변 검증과 정규화: `saveApplicationAnswers`

답변은 문항의 `displayOrder` 기준으로 매칭되며, `FieldType`별로 검증·정규화된다.

- **필수 문항 미응답**은 타입에 상관없이 `InvalidAnswerException`으로 거부된다.
- **CHECKBOX**는 선택값을 쉼표로 합치고, 필수인데 빈 선택이면 거부한다.
- **TIME_SLOT**은 날짜와 시간대 조각을 하나의 문자열로 재조립(`reassembleTimeSlots`)한 뒤,
  `Application.updatePreferInterviewInfo`로 지원자의 면접 희망 시간(`InterviewPreference`)을 갱신한다.

각 답변은 이메일 알림용 `AnswerEmailLine`으로도 함께 만들어져, 제출 직후 발행되는
`ApplicationSubmittedEvent`에 실려 [[notification-emails]]로 전달된다.

## 전형 단계 처리: `sendPassFailMessage`

동아리 운영진이 일괄 합격/불합격을 확정하면 `sendPassFailMessage(clubId, requestDto, stage)`가 단계별로
다르게 동작한다. 모든 단계에서, 아직 `Status.PENDING`인 지원서가 하나라도 남아 있으면
`PendingApplicationsExistException`을 던져 일부만 확정된 채로 다음 단계로 넘어가는 것을 막는다.

- **`Stage.INTERVIEW`(서류)**: 합격자 중 면접 일정(`interviewDate`/`interviewTime`)이 비어 있는 사람이
  있으면 `UnscheduledAcceptedApplicantExistsException`으로 거부한다. 통과하면 폼의 `interviewMessage`를
  저장하고, 합격자는 `Stage.FINAL`로 진행하며 다음 단계 평가를 위해 `Status`를 다시 `PENDING`으로
  리셋한 뒤 `InterviewApprovedEvent`를 발행한다. 불합격자는 `InterviewRejectedEvent`를 발행한 뒤
  `ClubMember`의 지원서 연결을 끊고(`clearApplicationByApplicationId`) `Application` 자체를 삭제한다.
- **`Stage.FINAL`(면접)**: 폼의 `finalMessage`를 저장한다. 합격자는 `Stage.RESULT`로 넘어가고, 이 시점에
  `ClubMemberRepository.updateRoleByApplicationId`로 `ClubMember.role`이 `APPLICANT`에서 `CLUB_MEMBER`로
  바뀐다 — **정식 동아리원 전환은 여기서 일어난다.** `FinalApprovedEvent`가 발행된다. 불합격자는
  `FinalRejectedEvent`를 발행하고 `ClubMember` 연결을 끊은 뒤 `Stage.RESULT`로만 옮기고(삭제하지 않음)
  지원 이력을 남긴다.
- **`Stage.RESULT`**: 해당 단계 지원서가 존재하는지만 확인하고 별다른 상태 전이는 하지 않는다(조회/조회
  가능 여부 확인용 종단 상태).
- **`stage == null`**: 면접이 없는 동아리를 위한 단축 경로로, 모든 단계의 지원자를 한 번에 평가한다.
  합격자는 곧바로 `CLUB_MEMBER`로 전환되고 `FinalApprovedEvent`가, 불합격자는 `FinalRejectedEvent`가
  발행된 뒤 `Application`이 삭제된다.

이 흐름 전체가 [[notification-emails]]에서 다루는 다섯 종류의 이벤트(`ApplicationSubmittedEvent`,
`InterviewApprovedEvent`, `InterviewRejectedEvent`, `FinalApprovedEvent`, `FinalRejectedEvent`)의 발행
지점이며, 각 이벤트는 트랜잭션 커밋 후 비동기로 지원자에게 이메일을 발송한다.
