---
type: architecture-concept
title: 핵심 도메인 모델
description: 동아리(Club)를 중심으로 지원폼, 지원서, 동아리원, 사용자가 어떻게 연결되는지 정리한 동아리움 백엔드의 핵심 엔티티 관계 문서.
tags: [domain-model, jpa, entity, club, application, club-member]
verified:
  - by: openwiki/0.4.3
    at: 2026-08-31T14:35:03.507Z
sources:
  - id: openwiki-source-b65859ef997e152c3d80c88a
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/domain/application/entity/Application.java
  - id: openwiki-source-235281bd512806bfb6a78398
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/domain/application/entity/Stage.java
  - id: openwiki-source-abae8ef56b85f16e77a1dbd1
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/domain/application/entity/Status.java
  - id: openwiki-source-82daa3fed862d98d1c7ee71f
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/domain/BaseEntity.java
  - id: openwiki-source-c57b2e8643dc184d5374f883
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/domain/clubApplyForm/entity/ClubApplyForm.java
  - id: openwiki-source-95e9dd3dc1b28540dbeeee70
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/domain/clubMember/entity/ClubMember.java
  - id: openwiki-source-4a4b83486d0f6f6568aae357
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/domain/clubMember/entity/ClubMemberProfile.java
  - id: openwiki-source-1e9db4be2eae1842f2694eb4
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/domain/clubMember/entity/Role.java
  - id: openwiki-source-9505c0e15c415e6293e311a6
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/domain/formQuestion/entity/FormQuestion.java
  - id: openwiki-source-f8a1a912ba8eaadc611f9705
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/domain/user/entity/User.java
generated: { by: "claude-code", at: "2026-08-31T14:35:03.507Z" }
---

# 핵심 도메인 모델

동아리움 백엔드는 "동아리(Club)가 지원폼(ClubApplyForm)을 만들고, 사용자(User)가 그 폼에 지원(Application)하며,
합격한 지원자가 동아리원(ClubMember)이 된다"는 하나의 축을 중심으로 도메인이 구성되어 있다. 모든 엔티티는
`BaseEntity`(`src/main/java/com/kakaotech/team18/backend_server/domain/BaseEntity.java`)를 상속해
`createdAt`/`lastModifiedAt`을 JPA Auditing(`AuditingEntityListener`)으로 자동 기록한다.

## 엔티티 관계 개요

```
User ──< Application >── ClubApplyForm ──1:1── Club
  │            │  │                              │
  │            │  └─< Answer >── FormQuestion ────┘
  │            └─< Comment (면접 평가)
  │
  └──< ClubMember >── Club
         │
         └─1:1─ ClubMemberProfile
```

- **Club** (`domain/club/entity/Club.java`): 동아리 마스터 엔티티. 이름·카테고리·모집 기간
  (`recruitStart`/`recruitEnd`)·면접 필요 여부와 면접 가능 기간(`interviewStartDate`~`interviewEndTime`)을
  가진다. `ClubIntroduction`을 1:1 컴포지션(`cascade = ALL, orphanRemoval = true`)으로 소유해 상세 소개를
  별도 테이블로 분리한다.
- **ClubApplyForm** (`domain/clubApplyForm/entity/ClubApplyForm.java`): 동아리 1개당 지원폼 1개(`@OneToOne`,
  `optional = false`)를 강제한다. 제목/설명 외에 전형 단계별 안내 메시지(`interviewMessage`, `finalMessage`)를
  들고 있어, 지원자에게 보여줄 문구를 폼 자체에서 관리한다.
- **FormQuestion** (`domain/formQuestion/entity/FormQuestion.java`): 지원폼에 속한 개별 질문. `FieldType`으로
  텍스트/객관식/시간대 선택 등 입력 형태를 구분하고, `options`는 `StringListConverter`로 구분자 직렬화된
  컬럼에 저장한다. `FieldType.TIME_SLOT`일 때만 `@ElementCollection`인 `TimeSlotOption` 목록을 사용하며,
  `updateFrom`은 필드 타입이 바뀌면 반대 종류의 데이터(`options` 또는 `timeSlotOptions`)를 명시적으로 null로
  비워 두 표현이 동시에 남지 않도록 한다.
- **Application** (`domain/application/entity/Application.java`): 한 사용자가 한 지원폼에 낸 지원서. 합격 여부인
  `Status`(PENDING/APPROVED/REJECTED)와 전형 진행 단계인 `Stage`(INTERVIEW/FINAL/RESULT)를 별도 축으로 가진다.
  즉 "지금 어느 전형 단계인지"와 "그 단계 결과가 어떻게 됐는지"가 독립적으로 관리된다. 면접 희망 시간은
  `interviewPreferences`(`InterviewPreference`, `cascade = ALL, orphanRemoval = true`)로 여러 개 보관하다가,
  실제 확정 일정은 `interviewDate`/`interviewTime` 두 컬럼에 확정값으로 저장한다.
- **Answer** (`domain/answer/entity/Answer.java`): `Application`과 `FormQuestion`을 다대일로 참조하는 조인
  엔티티로, 지원자가 각 질문에 작성한 답변 문자열을 담는다.
- **Comment**: `Application`에 달리는 면접관 평가/코멘트로, `Application.comments`에서
  `cascade = REMOVE, orphanRemoval = true`로 소유되어 지원서가 삭제되면 함께 삭제된다.
- **ClubMember** (`domain/clubMember/entity/ClubMember.java`): 사용자와 동아리를 연결하는 멤버십. `role`
  (APPLICANT/CLUB_MEMBER/CLUB_EXECUTIVE/CLUB_ADMIN/SYSTEM_ADMIN)로 권한 등급을, `activeStatus`
  (ACTIVE/INACTIVE)로 활동 여부를 관리한다. 어떤 `Application`을 통해 합류했는지 추적할 수 있도록
  `application`을 선택적 1:1로 참조한다.
- **ClubMemberProfile** (`domain/clubMember/entity/ClubMemberProfile.java`): `ClubMember`에 종속된 1:1
  상세 프로필(학번·학과·학적상태·가입일 등). `update()`는 각 필드별로 "새 값이 있으면 교체, 없으면 유지"하는
  부분 업데이트 패턴(`getOrDefault`)을 사용해, 일부 필드만 담긴 수정 요청도 안전하게 반영한다.
- **User** (`domain/user/entity/User.java`): 학번(`studentId`)·이메일·전화번호가 각각 unique 제약을 가진
  사용자 마스터. `kakaoId`는 nullable + unique로, 지원서 제출로 먼저 생성된 `User`가 이후 카카오 로그인으로
  연결(`connectKakaoId`)될 수 있음을 반영한다. `gender`/`faculty`는 선택 입력 필드로, `fillGenderIfAbsent`/
  `fillFacultyIfAbsent`는 값이 없을 때만 채워 넣어 재지원 시 기존에 입력된 값을 덮어쓰지 않는다.

## 설계 상 중요한 특징

- **User는 지원 경로를 통해서도 생성된다.** 지원서 제출 시 학번으로 기존 `User`를 조회해 재사용하므로,
  같은 학번의 사용자가 여러 동아리에 지원해도 `User` 레코드는 하나로 수렴한다. 이 재사용 때문에 `User`의
  선택 필드(`gender`, `faculty`)는 "없을 때만 채운다"는 규칙이 필요하다.
- **Application의 Status와 Stage는 독립 축이다.** 서비스 계층은 이 두 값을 각각 `updateStatus`/`updateStage`로
  갱신하며, 이 조합이 실제 전형 진행 워크플로를 구동한다(자세한 흐름은 [[recruitment-application]] 참고).
- **ClubMember는 Application과 분리된 생명주기를 가진다.** 합격한 지원자가 `ClubMember`로 전환된 뒤에도
  `Application`은 별도로 보존되어, 지원 이력과 현재 멤버십 상태를 동시에 조회할 수 있다.
