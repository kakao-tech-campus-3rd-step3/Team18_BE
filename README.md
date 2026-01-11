# Team18_BE

카카오테크캠퍼스 3기 Step3 - Team18 백엔드 서버

## 프로젝트 소개

동아리 관리 및 지원 시스템을 위한 Spring Boot 기반 REST API 서버입니다.

## 주요 기능

- **인증/인가**: Kakao OAuth2 로그인, JWT 기반 인증
- **동아리 관리**: 동아리 정보 조회, 카테고리별 검색, 모집 상태 관리
- **지원서 관리**: 동아리 지원서 작성, 제출, 상태 추적
- **지원자 관리**: 지원자 목록 조회, 상태 변경, 대시보드
- **리뷰 시스템**: 동아리 리뷰 작성 및 조회
- **댓글 기능**: 게시글 댓글 작성 및 관리
- **공지사항**: 동아리별 공지사항 관리
- **이메일 알림**: SMTP를 통한 지원 결과 이메일 발송

## 기술 스택

### Backend
- Java 21
- Spring Boot 3.5.5
- Spring Data JPA
- Spring Security
- Spring OAuth2 Client
- Spring Retry & AOP

### Database & Cache
- MySQL
- Redis
- H2 (개발 환경)

### Infrastructure
- AWS S3 (파일 저장소)
- Docker & Testcontainers

### Authentication
- JWT (io.jsonwebtoken:jjwt)
- Kakao OAuth2

### Email & Template
- Spring Mail (SMTP)
- Thymeleaf

### Documentation
- Swagger/OpenAPI 3.0 (Springdoc)

### Monitoring
- Spring Actuator
- Logback Discord Appender

### Test
- JUnit 5
- Spring Security Test
- Testcontainers (LocalStack)

## 시작하기

### 사전 요구사항

- Java 21 이상
- MySQL 8.0 이상
- Redis
- AWS S3 계정 (파일 업로드용)
- Kakao Developers 앱 등록 (OAuth2용)

### 환경 변수 설정

`application.properties` 또는 환경 변수로 다음 항목을 설정해야 합니다:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/your_database
spring.datasource.username=your_username
spring.datasource.password=your_password

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# JWT
jwt.secret=your_jwt_secret_key
jwt.access-token-expiration=3600000
jwt.refresh-token-expiration=604800000

# Kakao OAuth2
spring.security.oauth2.client.registration.kakao.client-id=your_kakao_client_id
spring.security.oauth2.client.registration.kakao.client-secret=your_kakao_client_secret
spring.security.oauth2.client.registration.kakao.redirect-uri=your_redirect_uri

# AWS S3
cloud.aws.credentials.access-key=your_aws_access_key
cloud.aws.credentials.secret-key=your_aws_secret_key
cloud.aws.s3.bucket=your_bucket_name
cloud.aws.region.static=your_region

# SMTP
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email
spring.mail.password=your_email_password
```

### 빌드 및 실행

```bash
# 빌드
./gradlew build

# 실행
./gradlew bootRun

# 또는 jar 파일 실행
java -jar build/libs/backend_server-0.0.1-SNAPSHOT.jar
```

## API 문서

서버 실행 후 Swagger UI를 통해 API 문서를 확인할 수 있습니다.

```
http://localhost:8080/swagger-ui.html
```

## 프로젝트 구조

```
src/main/java/com/kakaotech/team18/backend_server/
├── domain/
│   ├── application/        # 지원서 관리
│   ├── auth/              # 인증/인가
│   ├── club/              # 동아리 관리
│   ├── clubApplyForm/     # 지원서 양식
│   ├── clubMember/        # 동아리 회원
│   ├── clubReview/        # 동아리 리뷰
│   ├── comment/           # 댓글
│   ├── email/             # 이메일 발송
│   ├── formQuestion/      # 지원서 질문
│   ├── notices/           # 공지사항
│   └── user/              # 사용자
└── global/
    ├── config/            # 설정 (Security, Swagger, JPA, etc.)
    ├── converter/         # 데이터 변환기
    ├── dto/               # 공통 DTO
    ├── exception/         # 예외 처리
    └── security/          # JWT, 보안 설정
```

## 개발 팀

Kakao Tech Campus 3rd - Team 18

## 라이선스

이 프로젝트는 교육 목적으로 개발되었습니다.

## 링크

- [GitHub Repository](https://github.com/kakao-tech-campus-3rd-step3/Team18_BE)
