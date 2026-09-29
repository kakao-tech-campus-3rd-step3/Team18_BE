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
