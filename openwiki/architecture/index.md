# 파일

- [핵심 도메인 모델](domain-model.md) - 동아리(Club)를 중심으로 지원폼, 지원서, 동아리원, 사용자가 어떻게 연결되는지 정리한 동아리움 백엔드의 핵심 엔티티 관계 문서.
- [예외 처리와 에러 응답](error-handling.md) - CustomException 계층과 ErrorCode 카탈로그, GlobalExceptionHandler가 이를 일관된 HTTP 에러 응답으로 변환하는 방식, Spring Security 인증/인가 실패가 같은 경로로 합류하는 구조를 설명한다.
- [시스템 아키텍처 개요](overview.md) - 동아리움 백엔드의 계층 구조, 도메인 기준 패키지 구성, 외부 의존성(MySQL/Redis/S3/Kakao), 프로필별 설정, 그리고 전역 Spring 설정 클래스들의 역할을 정리한다.
