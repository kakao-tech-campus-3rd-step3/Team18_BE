# 앱 실행용 서비스 계정. 현행 EC2 인스턴스 프로파일(code-deploy-e2-role)에 대응한다.
resource "google_service_account" "app" {
  account_id   = "dongarium-app"
  display_name = "Cloud Run 백엔드 실행 계정"
}

# 앱이 읽어야 하는 비밀값만 허용한다.
resource "google_secret_manager_secret_iam_member" "app_secret_accessor" {
  for_each  = google_secret_manager_secret.app
  secret_id = each.value.id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${google_service_account.app.email}"
}

# Cloud SQL 접속과 로그·지표 기록.
resource "google_project_iam_member" "app" {
  for_each = toset([
    "roles/cloudsql.client",
    "roles/logging.logWriter",
    "roles/monitoring.metricWriter",
  ])

  project = var.project_id
  role    = each.value
  member  = "serviceAccount:${google_service_account.app.email}"
}

# ---------------------------------------------------------------------------
# GitHub Actions 배포용 (현행 AWS_ACCESS_KEY_ID/SECRET 대체)
# 장기 키를 만들지 않고 Workload Identity Federation으로 인증한다.
# ---------------------------------------------------------------------------
resource "google_service_account" "deployer" {
  account_id   = "dongarium-deployer"
  display_name = "GitHub Actions 배포 계정"
}

resource "google_iam_workload_identity_pool" "github" {
  workload_identity_pool_id = "github"
  display_name              = "GitHub Actions"
}

resource "google_iam_workload_identity_pool_provider" "github" {
  workload_identity_pool_id          = google_iam_workload_identity_pool.github.workload_identity_pool_id
  workload_identity_pool_provider_id = "github-oidc"

  attribute_mapping = {
    "google.subject"       = "assertion.sub"
    "attribute.repository" = "assertion.repository"
  }

  # 지정한 저장소에서 온 토큰만 받는다.
  attribute_condition = "attribute.repository in ${jsonencode(var.github_repository)}"

  oidc {
    issuer_uri = "https://token.actions.githubusercontent.com"
  }
}

resource "google_service_account_iam_member" "deployer_wif" {
  for_each = toset(var.github_repository)

  service_account_id = google_service_account.deployer.name
  role               = "roles/iam.workloadIdentityUser"
  member             = "principalSet://iam.googleapis.com/${google_iam_workload_identity_pool.github.name}/attribute.repository/${each.value}"
}

# 배포에 필요한 권한만 부여한다.
#   - artifactregistry.writer: 이미지 push (현행 ECR push)
#   - run.developer: Cloud Run 리비전 배포
#   - storage.objectAdmin: 프론트 정적 파일 업로드 (현행 aws s3 sync)
#   - iam.serviceAccountUser: 앱 서비스 계정으로 Cloud Run 배포
resource "google_project_iam_member" "deployer" {
  for_each = toset([
    "roles/artifactregistry.writer",
    "roles/run.developer",
    "roles/storage.objectAdmin",
  ])

  project = var.project_id
  role    = each.value
  member  = "serviceAccount:${google_service_account.deployer.email}"
}

resource "google_service_account_iam_member" "deployer_act_as_app" {
  service_account_id = google_service_account.app.name
  role               = "roles/iam.serviceAccountUser"
  member             = "serviceAccount:${google_service_account.deployer.email}"
}
