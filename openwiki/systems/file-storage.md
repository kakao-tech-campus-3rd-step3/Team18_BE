---
type: system-concept
title: 파일 업로드와 S3 연동
description: 동아리 이미지 업로드와 고아 이미지 정리를 담당하는 S3Service, 공지사항 첨부파일/양식 다운로드에 쓰이는 presigned URL 발급 경로, 두 버킷(bucket/bucket-attachments)의 역할 분리를 설명한다.
tags: [aws-s3, file-upload, presigned-url, notices]
verified:
  - by: openwiki/0.4.3
    at: 2026-08-31T14:35:03.507Z
sources:
  - id: openwiki-source-db63bf33aacbadf695f853f2
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/domain/club/service/OrphanImageCleanupService.java
  - id: openwiki-source-bccee861c153d3e49b1c1606
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/domain/clubMember/service/ClubMemberServiceImpl.java
  - id: openwiki-source-dc38e6c25b3590b004b9e48c
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/domain/files/entity/File.java
  - id: openwiki-source-15ead346052ba0de52938a88
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/domain/notices/service/NoticeServiceImpl.java
  - id: openwiki-source-4e9082ad1c68f2f0d32542a9
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/global/config/S3Config.java
  - id: openwiki-source-b988a540e4f51b630fde3ae0
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/global/scheduler/OrphanImageCleanupScheduler.java
  - id: openwiki-source-2ef8001a2fae0013ee56457f
    resource: repo://src/main/java/com/kakaotech/team18/backend_server/global/service/S3Service.java
generated: { by: "claude-code", at: "2026-08-31T14:35:03.507Z" }
---

# 파일 업로드와 S3 연동

이 서비스는 AWS S3 버킷을 용도별로 두 개 사용한다. `cloud.aws.s3.bucket`(동아리 이미지 전용)은 직접 업로드
API에 쓰이고, `cloud.aws.s3.bucket-attachments`(공지사항 첨부파일·양식 다운로드용)는 presigned URL로만
접근한다.

## S3 클라이언트 자격 증명 분기

`S3Config`(`global/config/S3Config.java`)는 `S3Client`와 `S3Presigner` 두 빈을 등록하면서, 로컬 개발과 EC2
운영 환경을 자동으로 구분한다. `cloud.aws.credentials.access-key`/`secret-key`가 설정되어 있으면
`StaticCredentialsProvider`로 고정 키를 사용하고(로컬), 비어 있으면 `InstanceProfileCredentialsProvider`로
EC2 인스턴스의 IAM Role 자격 증명을 자동으로 사용한다(운영). 이 덕분에 운영 환경에서는 AWS 키를 애플리케이션
설정에 담지 않아도 된다.

## 동아리 이미지 업로드: `S3Service`

`S3Service`(`global/service/S3Service.java`)는 `cloud.aws.s3.bucket` 버킷에 대해 업로드/삭제/전체 목록
조회를 제공한다.

- **업로드(`upload`)**: 빈 파일, `null` Content-Type, `image/jpeg`/`image/png`가 아닌 Content-Type을
  모두 `InvalidFileException`으로 거부한다. 파일명에서 경로 구분자(`\`, `/`)를 제거해 경로 조작을 막고,
  소문자로 정규화한 뒤 확장자가 `.png`/`.jpg`/`.jpeg`인지 다시 검증한다. 실제 저장 키는
  `UUID.randomUUID() + "-" + originalName`으로 만들어 파일명 충돌을 방지하며, 업로드 후 버킷의 정적
  URL(`https://{bucket}.s3.{region}.amazonaws.com/{key}`)을 그대로 반환해 이 URL이 DB에 저장되는 이미지
  경로가 된다.
- **삭제(`deleteFile`)**: 전달된 URL이 이 버킷의 표준 URL 형식으로 시작하는지 확인한 뒤 키를 추출해 삭제하며,
  형식이 다르면 삭제를 거부하고 `AwsS3Exception`을 던진다.
- **전체 목록(`listAllFiles`)**: `ListObjectsV2`를 continuation token으로 페이지네이션하며 버킷의 모든
  객체 URL을 모은다.

## 고아 이미지 정리

`OrphanImageCleanupService`(`domain/club/service/OrphanImageCleanupService.java`)는 `S3Service.listAllFiles()`로
얻은 전체 URL 목록에서 `ClubImageRepository.findAllImageUrls()`(DB에 실제로 참조되는 이미지 URL)에 없는 것만
"고아" 파일로 골라 하나씩 삭제한다. `OrphanImageCleanupScheduler`
(`global/scheduler/OrphanImageCleanupScheduler.java`)는 이 로직을 매일 새벽 3시(cron)에 실행하도록 작성되어
있지만, `@Scheduled` 애노테이션이 주석 처리되어 있어 **현재는 자동 실행되지 않는다**.

## 공지사항 첨부파일과 양식 다운로드: presigned URL

동아리 이미지와 달리, 공지사항 첨부파일과 동아리원 일괄 등록 양식(엑셀 템플릿)은 `cloud.aws.s3.bucket-attachments`
버킷을 사용하며 `S3Service`를 거치지 않고 각 서비스가 직접 `S3Presigner`를 호출한다.

- **공지사항 첨부파일**: `File` 엔티티(`domain/files/entity/File.java`)가 `Notice`에 다대일로 종속되어
  파일명·타입·`objectUri`를 저장한다. `NoticeServiceImpl.getNoticeById`는 공지사항 조회 시 각 첨부파일에
  대해 `GetObjectPresignRequest`(만료 7일, `Content-Disposition: attachment`)로 서명된 다운로드 URL을
  즉석에서 생성해 응답에 포함시킨다. 즉 첨부파일 URL은 매 조회마다 새로 발급되며 DB에는 원본 `objectUri`만
  저장된다.
- **동아리원 일괄 등록 양식**: `ClubMemberServiceImpl`도 같은 `bucket-attachments`에서
  `templates/members_template.xlsx` 키로 등록 양식 다운로드 URL을 제공한다.

## 파일 크기 제한

`application.yml`의 `spring.servlet.multipart` 설정으로 단일 파일 최대 5MB, 요청 전체(여러 파일 합계)
최대 50MB로 제한되며, 이를 초과하면 [[error-handling]]에서 다루는 `MaxUploadSizeExceededException` 핸들러가
`TOO_LARGE_FILE`(400)로 응답한다.
