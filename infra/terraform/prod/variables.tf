variable "project_id" {
  description = "GCP 프로젝트 ID"
  type        = string
}

variable "region" {
  description = "기본 리전"
  type        = string
  default     = "asia-northeast3"
}

variable "github_repository" {
  description = "GitHub Actions에서 배포를 허용할 저장소 (owner/repo 목록)"
  type        = list(string)
  default = [
    "kakao-tech-campus-3rd-step3/Team18_BE",
    "kakao-tech-campus-3rd-step3/Team18_FE",
  ]
}

variable "db_tier" {
  description = "Cloud SQL 사양. 현행 MySQL 컨테이너 사용량(약 412MB)과 DB 크기(4.5MB)를 기준으로 잡았다."
  type        = string
  default     = "db-g1-small"
}

variable "db_name" {
  description = "데이터베이스 이름 (현행 dongarium-db)"
  type        = string
  default     = "dongarium-db"
}

variable "db_user" {
  description = "앱이 사용하는 DB 계정 (현행 DB_USER)"
  type        = string
  default     = "team18"
}

variable "bucket_club_image" {
  description = "동아리 이미지 버킷 (현행 S3 dongarium-club-image)"
  type        = string
  default     = "dongarium-club-image"
}

variable "bucket_attachments" {
  description = "공지 첨부 버킷 (현행 S3 dongarium-attachments)"
  type        = string
  default     = "dongarium-attachments"
}

variable "bucket_frontend" {
  description = "프론트 정적 파일 버킷 (현행 프론트 S3 버킷)"
  type        = string
  default     = "dongarium-frontend"
}
