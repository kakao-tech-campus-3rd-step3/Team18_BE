# 파일

- [동아리 인기도 실시간 집계](club-popularity.md) - 조회수·체류시간(heartbeat)을 Redis 정렬 집합과 Lua 스크립트로 원자적으로 집계해 실시간 인기 동아리를 산정하고, 주기적으로 MySQL에 영속화하며 Redis 장애로부터 복구하는 시스템을 설명한다.
- [파일 업로드와 S3 연동](file-storage.md) - 동아리 이미지 업로드와 고아 이미지 정리를 담당하는 S3Service, 공지사항 첨부파일/양식 다운로드에 쓰이는 presigned URL 발급 경로, 두 버킷(bucket/bucket-attachments)의 역할 분리를 설명한다.
- [지원자 통계 집계](statistics.md) - 동아리 지원폼 단위로 성별·학부·입학연도·일자별 지원 추이를 집계하고, 재식별 방지를 위한 최소 공개 기준으로 마스킹하며, Redis cache-aside로 공개 통계를 캐싱하는 시스템을 설명한다.
