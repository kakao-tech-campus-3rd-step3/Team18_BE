# 현행 ECR(dongarium-server)에 대응한다.
resource "google_artifact_registry_repository" "server" {
  location      = var.region
  repository_id = "dongarium"
  description   = "백엔드 컨테이너 이미지 (현행 ECR dongarium-server 대체)"
  format        = "DOCKER"

  docker_config {
    immutable_tags = false
  }
}
