# 파일

- [카카오 로그인과 인증 세션](auth-and-session.md) - 카카오 OAuth2 인가 코드 교환부터 회원가입/로그인, 세 종류의 JWT(임시/액세스/리프레시), Redis 기반 리프레시 토큰 저장·로그아웃 블랙리스트, JWT 인증 필터와 메서드 단위 인가까지 전체 인증 흐름을 설명한다.
- [전형 결과 이메일 알림](notification-emails.md) - 지원서 제출과 각 전형 단계 결과를 트랜잭션 커밋 이후 비동기 도메인 이벤트로 처리해 HTML 이메일을 발송하고, SMTP 실패를 임시/영구로 분류해 Spring Retry로 재시도하는 알림 시스템을 설명한다.
- [동아리 지원 및 전형 프로세스](recruitment-application.md) - 지원폼 질문 구성부터 지원서 제출·답변 검증, 서류(INTERVIEW)·면접(FINAL)·결과(RESULT) 3단계 전형에 따른 Status/Stage 전이, 그리고 최종 합격자가 ClubMember(CLUB_MEMBER)로 전환되는 전체 흐름을 설명한다.
