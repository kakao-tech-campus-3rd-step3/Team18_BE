output "network" {
  description = "VPC 이름"
  value       = google_compute_network.main.name
}

output "subnetwork" {
  description = "서브넷 이름"
  value       = google_compute_subnetwork.main.name
}

output "artifact_registry" {
  description = "이미지 push 대상 (docker push <이 값>/<이미지>:<태그>)"
  value       = "${var.region}-docker.pkg.dev/${var.project_id}/${google_artifact_registry_repository.server.repository_id}"
}

output "app_service_account" {
  description = "Cloud Run 실행 계정"
  value       = google_service_account.app.email
}

output "deployer_service_account" {
  description = "GitHub Actions에 등록할 배포 계정 (GCP_SERVICE_ACCOUNT)"
  value       = google_service_account.deployer.email
}

output "workload_identity_provider" {
  description = "GitHub Actions에 등록할 공급자 (GCP_WORKLOAD_IDENTITY_PROVIDER)"
  value       = google_iam_workload_identity_pool_provider.github.name
}

output "secret_ids" {
  description = "값을 직접 입력해야 하는 Secret Manager 항목"
  value       = [for s in google_secret_manager_secret.app : s.secret_id]
}
